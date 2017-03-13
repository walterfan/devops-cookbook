
<!-- angularjs1,x example by walter -->
<!doctype html>
<html lang="en" ng-app="apiTestApp">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="Kanban">
    <meta name="author" content="Walter">
    <link rel="icon" href="./images/favicon.ico">

    <title>Kanban Web Service</title>

    <link rel="stylesheet" href="./static/css/bootstrap.css">
    <link rel="stylesheet" href="./static/css/app.css">
    <link rel="stylesheet" href="./static/css/ngDialog.min.css">
    <link rel="stylesheet" href="./static/css/ngDialog-theme-plain.min.css">
    <link rel="stylesheet" href="./static/css/ngDialog-theme-default.min.css">

    <link rel="stylesheet" href="./static/css/jumbotron-narrow.css">

    <script src="./static/js/vendor/angular.js"></script>
    <script src="./static/js/vendor/angular-sanitize.js"></script>
    <script src="./static/js/vendor/ui-bootstrap.js"></script>
    <script src="./static/js/vendor/ui-bootstrap-tpls.js"></script>
    <script src="./static/js/vendor/ngDialog.min.js"></script>

</head>
<body>
<div class="container">
    <div class="header clearfix">
        <nav>
            <ul class="nav nav-pills pull-right">
                <li role="presentation"><a href="/">Home</a></li>
                <li role="presentation" class="active"><a href="/http-tester.html">Guestbook</a></li>
            </ul>
        </nav>
        <h3 class="text-muted">Guestbook v1.0</h3>
    </div> <!-- header end -->


    <div class="panel panel-default" >

        <div class="page-header text-center">
            <h3> Look forward to your comments </h3>
        </div>

        <div class="panel-body">

            <form class="form-horizontal">
                <div class="form-group">
                    <label for="title" class="col-md-2 control-label">Title</label>
                    <div class="col-md-10">
                        <input type="text" class="form-control" id="title" placeholder="Title">
                    </div>
                </div>
                <div class="form-group">
                    <label for="content" class="col-md-2 control-label">Content</label>
                    <div class="col-md-10">
                        <textarea class="form-control" name="content" rows="4" placeholder="Content"></textarea>
                    </div>
                </div>
                <div class="form-group">
                    <label for="content" class="col-md-2 control-label">Author</label>

                    <div class="col-md-10">
                        <div class="form-group col-md-4 ">
                            <label for="name">Name</label>
                            <input type="text" class="form-control" id="name" placeholder="name">
                        </div>
                        <div class="form-group col-md-4">
                            <label for="email">Email</label>
                            <input type="email" class="form-control" id="email" placeholder="email">
                        </div>
                        <div class="form-group col-md-4">
                            <label for="phone">Phone</label>
                            <input type="text" class="form-control" id="phone" placeholder="phonenumber">
                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-md-10"></div>
                    <div class="col-md-2">
                        <input class="btn btn-success btn-lg" type="submit" value="Submit">
                    </div>
                </div>
            </form>
        </div> <!-- panel-body end -->

    </div> <!-- panel end -->
</div><!-- container end -->
<div style="text-align: center">

    <span>${copyRight}</span>

</div>
</body>
</html>
