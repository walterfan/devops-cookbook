<#import "layout.ftl" as layout>
<@layout.myLayout>
<div class="page-header text-center" xmlns="http://www.w3.org/1999/html">
        <h3> Sign In </h3>
    </div>

    <div class="panel-body">

        <form class="form-horizontal">
            <div class="form-group">
                <label for="title" class="col-md-2 control-label">Email</label>
                <div class="col-md-8">
                    <input type="text" class="form-control" id="title" placeholder="email">
                </div>
            </div>
            <div class="form-group">
                <label for="content" class="col-md-2 control-label">password</label>
                <div class="col-md-8">
                    <input type="password" class="form-control" id="title" placeholder="password">
                </div>
            </div>
            <div class="form-group">
                <div class="col-md-2">                </div>
                <div class="col-md-4">
                    <input type="checkbox" id="rememberMe" > Remember Me </input>
                </div>
                <div class="col-md-4">
                    <a href="/forgetpassword">forget password</a>
                </div>
            </div>

            <div class="form-group">
                <div class="col-md-10"></div>
                <div class="col-md-2">
                    <input class="btn btn-success btn-lg" type="submit" value="submit">
                </div>
            </div>
        </form>
    </div> <!-- panel-body end -->

<script>
    $('li:eq(2)').addClass('active');
</script>
</@layout.myLayout>





