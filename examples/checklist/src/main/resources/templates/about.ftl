<#import "layout.ftl" as layout>
<@layout.myLayout>
<div class="jumbotron">
    <p class="lead">
        Checklist for your work and life by Walter Fan
    </p>

</div>
<div>
    <ul>
        <li>Create a checklist</li>
        <li>Make a plan</li>
        <li>Create a memo</li>
        <li>Record a action list</li>

    </ul>

</div>
<script>
    $('li:eq(3)').addClass('active');
</script>
</@layout.myLayout>
