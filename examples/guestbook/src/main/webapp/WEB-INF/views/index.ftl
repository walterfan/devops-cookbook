<!DOCTYPE html>
<html ng-app="KanbanApp" >
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="Kanban">
    <meta name="author" content="Walter">
    <link rel="icon" href="./images/favicon.ico">

    <title>Kanban Web Service</title>

    <!-- Bootstrap core CSS -->
    <link href="./static/css/bootstrap.min.css" rel="stylesheet">

    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
    <link href="./static/css/ie10-viewport-bug-workaround.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="./static/css/jumbotron-narrow.css" rel="stylesheet">

    <script src="./static/js/vendor/angular.js"></script>
    <script src="./static/js/vendor/angular-sanitize.js"></script>
    <script src="./static/js/vendor/angular-resource.js"></script>
    <script src="./static/js/vendor/ui-bootstrap.js"></script>
    <script src="./static/js/vendor/ui-bootstrap-tpls.js"></script>

    <script src="./static/js/vendor/ngDialog.min.js"></script>

    <script src="./static/js/kanban-app.js"></script>

</head>

<body  ng-controller="KanbanController">

<div class="container" >
    <div class="header clearfix">
        <nav>
            <ul class="nav nav-pills pull-right">
                <li role="presentation" class="active"><a href="/">Home</a></li>
                <li role="presentation"><a href="guestbook">Guestbook</a></li>
                <li role="presentation"><a href="about">About</a></li>
            </ul>
        </nav>

        <h3 class="text-muted">Personal Information Manage System v1.0</h3>
    </div>

    <div class="jumbotron">
        <h2>Kanban</h2>
        <p class="lead">
            Kanban is the simplest method of agile
        </p>
        <p><a class="btn btn-lg btn-success" href="/app/callflow.html" role="button">Add a User Story</a></p>
    </div>

    <div class="row marketing">
        <div class="col-lg-12">
            <h4>Backlog</h4>

        </div>


    </div>

    <footer class="footer">
        <p>&copy; 2016 Kanban, Inc. <a href="mailto:walterfan@qq.com">Walter Fan</a>, All right reserved.</p>
    </footer>

</div> <!-- /container -->


<!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
<script src="./static/js/vendor/ie10-viewport-bug-workaround.js"></script>
</body>
</html>

