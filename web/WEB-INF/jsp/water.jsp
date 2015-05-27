<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Change water</title>
    </head>
    <body>


        <a href='s'>servlet</a> | <a href='http://imbi.ld.ttu.ee/tomcat_webapp_logs/t112818_MineralWater/log.txt'>log.txt</a> <br>

        <form action='s?action=save' method=POST>
            <table bgcolor='#000000' border=0 cellpadding=0 cellspacing=0>
                <tr>
                    <td>
                        <table width=100% border=0 cellpadding=2 cellspacing=1>
                            <TR BGCOLOR='#ffffff'><td BGCOLOR='#cccccc' nowrap>id:</td><td>&nbsp;${water.id}&nbsp;</TD></tr>
                            <TR BGCOLOR='#ffffff'><td BGCOLOR='#cccccc' nowrap>Name</td><td>&nbsp;<b><font color='#0000ff'><input type='text' value='${water.name}' name='name'></font></b>${formError.name}</TD></tr>
                            <TR BGCOLOR='#ffffff'><td BGCOLOR='#cccccc' nowrap>Mineralisation</td><td>&nbsp;<b><font color='#0000ff'><input type='text' value='${water.mineralisation}' name='mineralisation'></font></b>${formError.mineralisation}</TD></tr>
                            <TR BGCOLOR='#ffffff'><td BGCOLOR='#cccccc' nowrap>Content</td><td>&nbsp;<b><font color='#0000ff'><textarea name='content' cols=25 rows=6>${water.content}</textarea></font></b></TD></tr>
                        </table>
                    </td>
                </tr>
            </table>

            <input type="submit" value="salvesta" >
            <input type="hidden" name="id" value="${water.id}" />  
        </form>




    </body>
</html>