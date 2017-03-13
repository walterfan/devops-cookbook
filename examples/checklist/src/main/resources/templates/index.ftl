<#import "layout.ftl" as layout>
<@layout.myLayout>
<div class="jumbotron">
    <h2>Checklist</h2>
    <p class="lead">
        Checklist for your work and life
    </p>
    <p><a class="btn btn-lg btn-success" href="/checkist/add" role="button">Add a Check list</a></p>
</div>
<script>
    $('li:eq(0)').addClass('active');
</script>
</@layout.myLayout>





