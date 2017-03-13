<#import "layout.ftl" as layout>
<@layout.myLayout>
<!-- refer to http://bootsnipp.com/snippets/featured/login-and-register-tabbed-form -->
<div class="page-header text-center">
    <div class="row nav nav-tabs nav-justified">
        <div class="col-xs-6">
            <a href="#" class="active" id="login-form-link">Login</a>
        </div>
        <div class="col-xs-6">
            <a href="#" id="register-form-link">Register</a>
        </div>
    </div>
</div>

<div class="panel-body" ng-app="myApp" ng-controller="myController">
    <div class="row">
        <div class="col-lg-12">
            <form id="login-form" class="form-horizontal" ng-submit="submitLoginForm()">
                <div class="form-group">
                    <input type="text" name="username" id="username" tabindex="1" class="form-control" placeholder="Username" value=""  ng-model="user.username">
                </div>
                <div class="form-group">
                    <input type="password" name="password" id="password" tabindex="2" class="form-control" placeholder="Password"  ng-model="user.password">
                </div>
                <div class="form-group text-center">
                    <input type="checkbox" tabindex="3" class="" name="remember" id="remember">
                    <label for="remember"> Remember Me</label>
                </div>
                <div class="form-group">
                    <div class="row">
                        <div class="col-sm-6 col-sm-offset-3">
                            <input type="submit" name="login-submit" id="login-submit" tabindex="4" class="form-control btn btn-login" value="Log In">
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="row">
                        <div class="col-lg-12">
                            <div class="text-center">
                                <a href="http://phpoll.com/recover" tabindex="5" class="forgot-password">Forgot Password?</a>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
            <form id="register-form" class="form-horizontal" style="display: none;" ng-submit="submitRegisterForm()">
                <div class="form-group">
                    <input type="text" name="username" id="username" tabindex="1" class="form-control" placeholder="Username" value=""  ng-model="user.username">
                </div>
                <div class="form-group">
                    <input type="email" name="email" id="email" tabindex="1" class="form-control" placeholder="Email Address" value=""  ng-model="user.email">
                </div>
                <div class="form-group">
                    <input type="password" name="password" id="password" tabindex="2" class="form-control" placeholder="Password"  ng-model="user.password">
                </div>
                <div class="form-group">
                    <input type="password" name="confirm-password" id="confirm-password" tabindex="2" class="form-control" placeholder="Confirm Password"  ng-model="user.passwordConfirmation">
                </div>
                <div class="form-group">
                    <div class="row">
                        <div class="col-sm-6 col-sm-offset-3">
                            <input type="submit" name="register-submit" id="register-submit" tabindex="4" class="form-control btn btn-register" value="Register Now">
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div> <!-- panel-body end -->

<script>
    $('li:eq(1)').addClass('active');
</script>
</@layout.myLayout>





