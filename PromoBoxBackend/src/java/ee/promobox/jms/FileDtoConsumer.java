package ee.promobox.jms;

import java.io.File;
import java.io.FilenameFilter;
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
	
	public boolean createMultipageCampaingsFiles(CampaignsFiles cFile) {
		boolean hasManyFiles = false;
		
		File clientDir = fileService.getClientFolder(cFile.getClientId());
		
		final String outputFileName = fileService.getOutputFile(cFile.getClientId(), cFile.getFileId(), null)
				.getName();
		File[] pagesFiles = clientDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(outputFileName + "-") 
						&& !name.contains("port");
			}
		});

		if (pagesFiles.length > 1) {
			hasManyFiles = true;
			for (int i = 0; i < pagesFiles.length; i++) {
				try {
					File pageFile = pagesFiles[i];
					String filename = pageFile.getName();
					log.info("Process file: " + filename);
					String[] part = filename.split("-");
					int page = Integer.parseInt(part[1]);
					
					String name = cFile.getFilename();
					
					if (cFile.getPage() == null || // Means that file was not before proccessed to create pages from it
							userService.findFileByIdAndPage(cFile.getFileId(), cFile.getPage()) == null) {
						if (page == 0) {
							cFile.setPage(0);
							cFile.setFilename(name + "[0]");
							
							cFile.setSize( (int) pageFile.length());
							userService.updateCampaignFile(cFile);
						} else {
							CampaignsFiles pageCampaignsFiles = new CampaignsFiles();
							pageCampaignsFiles.setAdCampaignsId(cFile.getAdCampaignsId());
							pageCampaignsFiles.setClientId(cFile.getClientId());
							pageCampaignsFiles.setCreatedDt(cFile.getCreatedDt());
							pageCampaignsFiles.setFileId(cFile.getFileId());
							pageCampaignsFiles.setFilename(name + "[" + page + "]");
							pageCampaignsFiles.setFileType(cFile.getFileType());
							pageCampaignsFiles.setPage(page);
							pageCampaignsFiles.setSize( (int) pageFile.length());
							pageCampaignsFiles.setStatus(cFile.getStatus());
							
							userService.addCampaignFile(pageCampaignsFiles);
							pageCampaignsFiles.setOrderId(pageCampaignsFiles.getId());
							userService.updateCampaignFile(pageCampaignsFiles);
							
							log.info("Created new campaign file: " + pageCampaignsFiles.getId());
						}
					}
					
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		
		return hasManyFiles;
	}

	public void handleMessage(FileDto fileDto) {

		CampaignsFiles cFile = userService.findCampaignFileById(fileDto
				.getCampaignFileId());

		fileDto.setFileId(cFile.getFileId());

		boolean result = convertFile(fileDto);

		if (result) {
			cFile.setStatus(CampaignsFiles.STATUS_ACTIVE);
			
			boolean manyFiles = createMultipageCampaingsFiles(cFile);

			File mp4File = fileService.getOutputMp4File(fileDto.getClientId(), cFile.getFileId());
			if (mp4File.exists()) {
				cFile.setSize((int) mp4File.length());
			} else if (!manyFiles) {
			
				File file = fileService.getOutputFile(fileDto.getClientId(),
						cFile.getFileId(), null);
				cFile.setSize((int) file.length());
			} // otherwise all file sizes set in createMultipageCampaingsFiles procedure

			Files dbFile = userService.findFileById(cFile.getFileId());

			userService.updateCampaignFile(cFile);

			AdCampaigns camp = userService.findCampaignByCampaignId(cFile
					.getAdCampaignsId());
			camp.setUpdateDate(new Date());

			if (!fileDto.isRotate()) {
				camp.setCountFiles(camp.getCountFiles() + 1);
				if (fileDto.getFileType() == FileTypeUtils.FILE_TYPE_AUDIO) {
					camp.setCountAudios(camp.getCountAudios() + 1);
					camp.setAudioLength(camp.getAudioLength()
							+ dbFile.getContentLength());
				} else if (fileDto.getFileType() == FileTypeUtils.FILE_TYPE_VIDEO) {
					camp.setCountVideos(camp.getCountVideos() + 1);
					camp.setVideoLength(camp.getVideoLength()
							+ dbFile.getContentLength());
				} else {
					camp.setCountImages(camp.getCountImages() + 1);
				}
			}

			userService.updateCampaign(camp);
		}
	}

	public boolean convertFile(FileDto f) {
		String type = f.getExtention();

		boolean result = false;

		switch (type.toUpperCase()) {
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
		case "WAV":
			result = convertAudio(f);
			break;
		case "MP3":
			result = convertAudio(f);
			break;
		case "MP4":
			result = convertVideo(f);
			break;
		case "M2TS":
			result = convertVideo(f);
			break;
		case "AAC":
			result = convertAudio(f);
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
			result = convertOfficeDocument(f);
			break;
		}

		return result;
	}

	private boolean convertOfficeDocument(FileDto f) {
		int clientId = f.getClientId();
		int fileId = f.getFileId();

		File rawFile = fileService.getRawFile(clientId, fileId);
		File outputFile = fileService.getOutputFile(clientId, fileId, null);
		File thumbFile = fileService.getThumbFile(clientId, fileId, null);

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
		//imageConvert.flatten();
		imageConvert.input(outputFile);
		//imageConvert.page(0);
		imageConvert.background("white");
		imageConvert.resize(1920, 1920, false, true, false);
		imageConvert.rotate(f.getAngle());

		imageConvert.outputFormat("jpg");

		if (imageConvert.processToFile(outputFile)) {

			imageConvert = new ImageOP(config.getImageMagick());

			imageConvert.input(outputFile);

			imageConvert.resize(250, 250);

			imageConvert.background("white");
			imageConvert.gravity("center");
			
			imageConvert.outputFormat("png");

			imageConvert.processToFile(thumbFile);

			return true;
		}

		return false;
	}

	private boolean convertPdf(FileDto f) {
		try {
			int clientId = f.getClientId();
			int fileId = f.getFileId();
	
			File rawFile = fileService.getRawFile(clientId, fileId);
			File outputFile = fileService.getOutputFile(clientId, fileId, null);
			File thumbFile = fileService.getThumbFile(clientId, fileId, null);
	
			ImageOP imageConvert = new ImageOP(config.getImageMagick());
	
			imageConvert.density(300);
			//imageConvert.flatten();
			imageConvert.input(rawFile);
			//imageConvert.page(0);
			imageConvert.background("white");
			imageConvert.resize(1920, 1920, false, true, false);
			imageConvert.rotate(f.getAngle());
	
			imageConvert.outputFormat("jpg");
	
			if (imageConvert.processToFile(outputFile)) {
	
				imageConvert = new ImageOP(config.getImageMagick());
	
				imageConvert.input(rawFile);
	
				imageConvert.resize(250, 250);
	
				imageConvert.background("white");
				imageConvert.gravity("center");
				
				imageConvert.outputFormat("png");
				//imageConvert.extent("250x250");
	
				imageConvert.processToFile(thumbFile);
	
				return true;
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}

		return false;
	}

	private boolean copyFile(FileDto f) {
		int clientId = f.getClientId();
		int fileId = f.getFileId();

		File rawFile = fileService.getRawFile(clientId, fileId);
		File outputFile = fileService.getOutputFile(clientId, fileId, null);

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
		File outputFile = fileService.getOutputFile(clientId, fileId, null);
		File thumbFile = fileService.getThumbFile(clientId, fileId, null);

		ImageOP imageConvert = new ImageOP(config.getImageMagick());

		imageConvert.input(rawFile);
		imageConvert.outputFormat("jpg");
		imageConvert.resize(1920, 1920, false, true, false);
		imageConvert.rotate(f.getAngle());

		imageConvert.processToFile(outputFile);

		imageConvert = new ImageOP(config.getImageMagick());

		imageConvert.input(rawFile);
		imageConvert.outputFormat("png");
		imageConvert.resize(250, 250);
		imageConvert.rotate(f.getAngle());

		imageConvert.background("white");
		imageConvert.gravity("center");
		// imageConvert.extent("250x250");

		return imageConvert.processToFile(thumbFile);
	}


	private boolean convertAudio(FileDto f) {
		int clientId = f.getClientId();
		int fileId = f.getFileId();

		File rawFile = fileService.getRawFile(clientId, fileId);
		File outputFile = fileService.getOutputFile(clientId, fileId, null);
		
		VideoOP videoConvert = new VideoOP(config.getAvconv());
		return videoConvert.input(rawFile)
			.codecAudio("libmp3lame")
			.bitrateAudio("128k")
			.format("mp3")
			.strict("experimental")
			.overwrite()
			.processToFile(outputFile);
	}
	
	private boolean convertVideo(FileDto f) {
		int clientId = f.getClientId();
		int fileId = f.getFileId();

		File rawFile = fileService.getRawFile(clientId, fileId);
		File outputFile = fileService.getOutputFile(clientId, fileId, null);
		File outputMp4File = fileService.getOutputMp4File(clientId, fileId);
		File thumbFile = fileService.getThumbFile(clientId, fileId, null);

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
		//imageConvert.extent("250x250");

		if (f.getAngle() != 0) {
			imageConvert.rotate(f.getAngle());
		}

		imageConvert.processToFile(thumbFile);

		boolean result = convertVideo(rawFile, outputFile, f.getAngle(), "libvpx", "webm");
		if (result) {
			result = convertVideo(rawFile, outputMp4File, f.getAngle(), "libx264", "mp4");
		}

		return result;
	}
	
	
	private boolean convertVideo(File raw, File output, int angle, String codec, String format) {
		VideoOP videoConvert = new VideoOP(config.getAvconv());

		if (angle == 0) {
			videoConvert.input(raw).codecVideo(codec).scale("-1:720")
					.bitrateVideo("2M").maxrate("2M").format(format)
					.strict("experimental").overwrite();

			return videoConvert.processToFile(output);

		} else {
			videoConvert = new VideoOP(config.getAvconv());
			videoConvert.input(raw).codecVideo(codec).bitrateVideo("2M")
					.maxrate("2M").format(format).strict("experimental").overwrite();

			if (angle == 90) {
				videoConvert.vf("scale=-1:720", "transpose=1");
			} else if (angle == 180) {
				videoConvert.vf("scale=-1:720", "transpose=1,transpose=1");
			} else if (angle == 270) {
				videoConvert.vf("scale=-1:720", "transpose=2");
			}

			return videoConvert.processToFile(output);
		}
	}

}
