angular.module("agileArticle")
    .controller("agileArticleCtrl", function($scope) {
        $scope.data = {
            articles: [
                { topic: "A glance of Angular", author: "Walter", content:" Angular is a JS framework"},
                { topic: "A glance of Spring MVC", author: "Walter", content:" Spring MVC is a MVC framework"},
                { topic: "A glance of Spring Security", author: "Walter", content:" Spring Security is a Security framework"}
            ]
        };
    });