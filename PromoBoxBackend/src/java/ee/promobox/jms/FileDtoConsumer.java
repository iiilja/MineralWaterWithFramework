package ee.promobox.jms;

import java.io.File;
import java.util.Date;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ee.promobox.KioskConfig;
import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.CampaignsFiles;
import ee.promobox.entity.Files;
import ee.promobox.service.FileService;
import ee.promobox.service.UserService;
import ee.promobox.util.FileTypeUtils;
import ee.promobox.util.ImageOP;
import ee.promobox.util.UnoconvOP;
import ee.promobox.util.VideoOP;

public class FileDtoConsumer implements Runnable {

	private static final Log log = LogFactory.getLog(FileDtoConsumer.class);

	private ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://kioskBroker");;
	
	private int clientId;

	private KioskConfig config;
	private UserService userService;
	private FileService fileService;

	public FileDtoConsumer(int clientId, KioskConfig config,
			UserService userService, FileService fileService) {
		super();

		this.clientId = clientId;
		this.config = config;
		this.userService = userService;
		this.fileService = fileService;
	}

	public void run() {
		Connection connection;
		try {
			connection = connectionFactory.createConnection();
			connection.start();

			// Create a Session
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			Destination d = session.createQueue("fileDestination" + clientId);

			// Create a MessageConsumer from the Session to the Topic or Queue
			MessageConsumer consumer = session.createConsumer(d);

			// Wait for a message
			Message message = null;
			while (true) {
				message = consumer.receive(1000);

				if (message != null) {
					break;
				}
			}

			if (message instanceof ObjectMessage) {
				FileDto fileDto = (FileDto) ((ObjectMessage) message)
						.getObject();

				handleMessage(fileDto);
			}

			consumer.close();
			session.close();
			connection.close();

		} catch (JMSException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
		}

	}

	public void handleMessage(FileDto fileDto) {
		log.info("Converting file:");

		log.info("Campaign File id: " + fileDto.getCampaignFileId());
		log.info("File extention: " + fileDto.getExtention());

		CampaignsFiles cFile = userService.findCampaignFileById(fileDto
				.getCampaignFileId());
		log.info(" cFile: " + cFile);
		log.info(" File id: " + cFile.getFileId());
		fileDto.setFileId(cFile.getFileId());

		boolean result = convertFile(fileDto);

		if (result) {
			cFile.setStatus(CampaignsFiles.STATUS_ACTIVE);

			File file = fileService.getOutputFile(fileDto.getClientId(),
					cFile.getFileId());
			cFile.setSize((int) file.length());

			Files dbFile = userService.findFileById(cFile.getFileId());

			userService.updateCampaignFile(cFile);

			AdCampaigns camp = userService.findCampaignByCampaignId(cFile
					.getAdCampaignsId());
			camp.setUpdateDate(new Date());

			camp.setCountFiles(camp.getCountFiles() + 1);
			if (fileDto.getFileType() == FileTypeUtils.FILE_TYPE_IMAGE) {
				camp.setCountImages(camp.getCountImages() + 1);
			} else if (fileDto.getFileType() == FileTypeUtils.FILE_TYPE_AUDIO) {
				camp.setCountAudios(camp.getCountAudios() + 1);
				camp.setAudioLength(camp.getAudioLength()
						+ dbFile.getContentLength());
			} else if (fileDto.getFileType() == FileTypeUtils.FILE_TYPE_VIDEO) {
				camp.setCountVideos(camp.getCountVideos() + 1);
				camp.setVideoLength(camp.getVideoLength()
						+ dbFile.getContentLength());
			}

			userService.updateCampaign(camp);
		}
	}

	public boolean convertFile(FileDto f) {
		String type = f.getExtention();

		boolean result = false;

		switch (type.toUpperCase()) {
		case "DOC":
			result = convertOfficeDocument(f);
			break;
		case "DOCX":
			result = convertOfficeDocument(f);
			break;
		case "PPT":
			result = convertOfficeDocument(f);
			break;
		case "XLS":
			result = convertOfficeDocument(f);
			break;
		case "JPG":
			result = convertImage(f);
			break;
		case "JPEG":
			result = convertImage(f);
			break;
		case "PNG":
			result = convertImage(f);
			break;
		case "BMP":
			result = convertImage(f);
			break;
		case "MP3":
			result = copyFile(f);
			break;
		case "MP4":
			result = convertVideo(f);
			break;
		case "M2TS":
			result = convertVideo(f);
			break;
		case "AAC":
			result = copyFile(f);
			break;
		case "AVI":
			result = convertVideo(f);
			break;
		case "MOV":
			result = convertVideo(f);
			break;
		case "WMV":
			result = convertVideo(f);
			break;
		case "PDF":
			result = convertPdf(f);
			break;
		default:
			break;
		}

		return result;
	}

	private boolean convertOfficeDocument(FileDto f) {
		int clientId = f.getClientId();
		int fileId = f.getFileId();

		File rawFile = fileService.getRawFile(clientId, fileId);
		File outputFile = fileService.getOutputFile(clientId, fileId);
		File outputPortFile = fileService.getOutputPortFile(clientId, fileId);
		File thumbFile = fileService.getThumbFile(clientId, fileId);

		File rawFileWithExt = null;
		try {
			rawFileWithExt = new File(rawFile.getCanonicalPath() + "."
					+ f.getExtention());

			FileUtils.copyFile(rawFile, rawFileWithExt);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return false;
		}

		UnoconvOP unoconv = new UnoconvOP(config.getUnoconv());
		unoconv.output(outputFile);
		unoconv.format("pdf");
		unoconv.input(rawFileWithExt);
		unoconv.processToFile();

		ImageOP imageConvert = new ImageOP(config.getImageMagick());
		imageConvert.density(300);
		imageConvert.flatten();
		imageConvert.input(outputFile);
		imageConvert.page(0);
		imageConvert.background("white");
		imageConvert.resize(1920, 1920);
		imageConvert.rotate(f.getRotate());

		imageConvert.outputFormat("png");

		if (imageConvert.processToFile(outputFile)) {

			imageConvert = new ImageOP(config.getImageMagick());

			imageConvert.input(rawFile);

			imageConvert.rotate(270 + f.getRotate());

			imageConvert.outputFormat("png");

			imageConvert.processToFile(outputPortFile);

			imageConvert = new ImageOP(config.getImageMagick());

			imageConvert.input(outputFile);

			imageConvert.resize(250, 250);

			imageConvert.background("white");
			imageConvert.gravity("center");
			imageConvert.extent("250x250");

			imageConvert.processToFile(thumbFile);

			return true;
		}

		return false;
	}

	private boolean convertPdf(FileDto f) {
		int clientId = f.getClientId();
		int fileId = f.getFileId();

		File rawFile = fileService.getRawFile(clientId, fileId);
		File outputFile = fileService.getOutputFile(clientId, fileId);
		File outputPortFile = fileService.getOutputPortFile(clientId, fileId);
		File thumbFile = fileService.getThumbFile(clientId, fileId);

		ImageOP imageConvert = new ImageOP(config.getImageMagick());

		imageConvert.density(300);
		imageConvert.flatten();
		imageConvert.input(rawFile);
		imageConvert.page(0);
		imageConvert.background("white");
		imageConvert.resize(1920, 1920);
		imageConvert.rotate(f.getRotate());

		imageConvert.outputFormat("png");

		if (imageConvert.processToFile(outputFile)) {

			imageConvert = new ImageOP(config.getImageMagick());

			imageConvert.input(rawFile);

			imageConvert.rotate(270 + f.getRotate());

			imageConvert.outputFormat("png");

			imageConvert.processToFile(outputPortFile);

			imageConvert = new ImageOP(config.getImageMagick());

			imageConvert.input(outputFile);

			imageConvert.resize(250, 250);

			imageConvert.background("white");
			imageConvert.gravity("center");
			imageConvert.extent("250x250");

			imageConvert.processToFile(thumbFile);

			return true;
		}

		return false;
	}

	private boolean copyFile(FileDto f) {
		int clientId = f.getClientId();
		int fileId = f.getFileId();

		File rawFile = fileService.getRawFile(clientId, fileId);
		File outputFile = fileService.getOutputFile(clientId, fileId);

		try {
			FileUtils.copyFile(rawFile, outputFile);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return false;
		}

		return true;
	}

	private boolean convertImage(FileDto f) {
		int clientId = f.getClientId();
		int fileId = f.getFileId();

		File rawFile = fileService.getRawFile(clientId, fileId);
		File outputFile = fileService.getOutputFile(clientId, fileId);
		File outputPortFile = fileService.getOutputPortFile(clientId, fileId);
		File thumbFile = fileService.getThumbFile(clientId, fileId);

		ImageOP imageConvert = new ImageOP(config.getImageMagick());

		imageConvert.input(rawFile);
		imageConvert.outputFormat("png");
		imageConvert.resize(1920, 1920);
		imageConvert.rotate(f.getRotate());

		imageConvert.processToFile(outputFile);

		imageConvert = new ImageOP(config.getImageMagick());

		imageConvert.input(rawFile);
		imageConvert.outputFormat("png");
		imageConvert.rotate(270 + f.getRotate());

		imageConvert.processToFile(outputPortFile);

		imageConvert = new ImageOP(config.getImageMagick());

		imageConvert.input(rawFile);
		imageConvert.outputFormat("png");
		imageConvert.resize(250, 250);
		imageConvert.rotate(f.getRotate());

		imageConvert.background("white");
		imageConvert.gravity("center");
		// imageConvert.extent("250x250");

		return imageConvert.processToFile(thumbFile);
	}

	private boolean convertVideo(FileDto f) {
		int clientId = f.getClientId();
		int fileId = f.getFileId();

		File rawFile = fileService.getRawFile(clientId, fileId);
		File outputFile = fileService.getOutputFile(clientId, fileId);
		File thumbFile = fileService.getThumbFile(clientId, fileId);

		VideoOP videoConvert = new VideoOP(config.getAvconv());

		videoConvert.input(rawFile);
		videoConvert.thumbnail();
		videoConvert.scale("500:-1");
		videoConvert.format("image2");

		videoConvert.processToFile(thumbFile);

		ImageOP imageConvert = new ImageOP(config.getImageMagick());

		imageConvert.input(thumbFile);
		imageConvert.outputFormat("png");
		imageConvert.resize(250, 250);

		imageConvert.background("white");
		imageConvert.gravity("center");
		imageConvert.extent("250x250");

		if (f.getRotate() != 0) {
			imageConvert.rotate(f.getRotate());
		}

		imageConvert.processToFile(thumbFile);

		boolean result = false;
		videoConvert = new VideoOP(config.getAvconv());

		if (f.getRotate() == 0) {
			videoConvert.input(rawFile).codecVideo("libvpx").scale("-1:720")
					.bitrateVideo("1M").maxrate("1M").format("webm");

			result = videoConvert.processToFile(outputFile);

		} else {
			videoConvert = new VideoOP(config.getAvconv());
			videoConvert.input(rawFile).codecVideo("libvpx").bitrateVideo("1M")
					.maxrate("1M").format("webm").overwrite();

			if (f.getRotate() == 90) {
				videoConvert.vf("scale=-1:720", "transpose=1");
			} else if (f.getRotate() == 180) {
				videoConvert.vf("scale=-1:720", "transpose=1,transpose=1");
			} else if (f.getRotate() == 270) {
				videoConvert.vf("scale=-1:720", "transpose=2");
			}

			result = videoConvert.processToFile(outputFile);
		}

		return result;
	}

}
