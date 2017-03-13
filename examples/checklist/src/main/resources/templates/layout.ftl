<#macro myLayout>
<!DOCTYPE html>
<html >
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="Kanban">
    <meta name="author" content="Walter">
    <link rel="icon" href="./images/favicon.ico">

    <title>Check List</title>

    <!-- Bootstrap core CSS -->
    <link href="./css/bootstrap.min.css" rel="stylesheet">

    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
    <link href="./css/ie10-viewport-bug-workaround.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="./css/app.css" rel="stylesheet">
    <link href="./css/jumbotron-narrow.css" rel="stylesheet">
    <script src="./js/vendor/jquery-1.11.2.min.js"></script>

    <script src="./js/vendor/angular.js"></script>
    <script src="./js/vendor/angular-sanitize.js"></script>
    <script src="./js/vendor/angular-resource.js"></script>
    <script src="./js/vendor/ui-bootstrap.js"></script>
    <script src="./js/vendor/ui-bootstrap-tpls.js"></script>

    <script src="./js/vendor/ngDialog.min.js"></script>

    <script src="./js/app.js"></script>

</head>

<body>

<div class="container" >
    <#include "header.ftl"/>

    <div class="panel panel-default" >
    <#nested/>
    </div>

    <#include "footer.ftl"/>

</div> <!-- /container -->


<!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
<script src="./js/vendor/ie10-viewport-bug-workaround.js"></script>
</body>
</html>

</#macro>