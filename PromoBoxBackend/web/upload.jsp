<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->
<html>
    <head>
        <title>Upload a file</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
    </head>
    <body>
        <form action="/service/files/upload?token=78702be4d1f54b6cb442d86494153381&campaignId=1" method="POST" enctype="multipart/form-data">
            <input type="file" name="file"><br>
            <input type="submit" value="Submit">
        </form>
    </body>
</html>
