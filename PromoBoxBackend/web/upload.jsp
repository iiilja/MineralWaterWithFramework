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
        <form action="/service/token/096ca54381ee401184d39a9eaf73d8be/files/1" method="POST" enctype="multipart/form-data">
            <input type="file" name="file"><br>
            <input type="submit" value="Submit">
        </form>
    </body>
</html>
