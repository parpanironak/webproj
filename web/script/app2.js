var app2 = angular.module('app2', ['autocomplete']);

    app2.controller('MyCtrl', function($scope, MovieRetriever){
        $scope.movies = ["Lord of the Rings",
                        "Drive",
                        "Science of Sleep",
                        "Back to the Future",
                        "Oldboy"];

		
        // gives another movie array on change
        $scope.updateMovies = function(typed){
            // MovieRetriever could be some service returning a promise
            console.log(typed)
            $scope.newmovies.then(function(data){
              $scope.movies = data;
            });
        }
    });