var app = angular.module('app', ['autocomplete']);


app.controller('MyCtrl', function($scope, $http){

  $scope.suggest = []
  $scope.query = ""
  $scope.data = []
  $scope.total = 0
  $scope.username = "ronak";
  $scope.next = {start:0, end:-1, query:""}
  $scope.prev = {start:0, end:-1, query:""}
  $scope.nextflag = true
  $scope.prevflag = true
  $scope.results = []
  $scope.doSomething = function(typedthings, user){
    //console.log("Do something like reload data with this: " + typedthings );
	console.log("do something user test:" + user)
	if(typedthings.length > 0)
    {
		var temp = ""
		
		
		if(!user)
			temp = "http://localhost:25809/instant?query="+typedthings
		else if(user.length <= 0)
			temp = "http://localhost:25809/instant?query="+typedthings 
		else
			temp = "http://localhost:25809/instant?query="+typedthings+"&username="+user;
		
		$http.get(temp).success(function(response) 
		{$scope.suggest = response.data});
	}
	else
		$scope.suggest = []
		
	$scope.results = []	
  }

  $scope.doSomethingElse = function(suggestion){
    console.log("Suggestion selected: " + suggestion );
  }
  
  $scope.searchrequest = function(query){
  	console.log("Suggestion selected: " + query);
	if(query.length > 0)
    	$http.get("http://localhost:25809/search?query="+query+"&ranker=comprehensive").success(
		function(response) 
			{
				$scope.data = response.data;
			 	$scope.total = response.total;
				$scope.next = {}
  				$scope.prev = {}			
			 	
			});
  }
  
  $scope.searchrequestrange = function(query, start, end, user){
	

	console.log(user)	
	
  	console.log("Suggestion selected: " + query);
	if(query.length > 0)
	{
		var temp = ""
		if(!user)
			temp = "http://localhost:25809/search?query="+query+"&ranker=comprehensive&start="+start+"&end="+end;
		else if(user.length <= 0)
			temp = "http://localhost:25809/search?query="+query+"&ranker=comprehensive&start="+start+"&end="+end;
		else
			temp = "http://localhost:25809/search?query="+query+"&ranker=comprehensive&start="+start+"&end="+end+"&username="+user;			
				
    	$http.get(temp).success(
		function(response) 
			{
				$scope.data = response.data;
			 	$scope.total = response.total;
			 	$scope.next = {start:0, end:-1, query:query}
  				$scope.prev = {start:0, end:-1, query:query}	
				
				if(start > 0)
				{
					$scope.prev.end = start;
					$scope.prev.start = start - 10 > 0 ? start - 10 : 0;
					$scope.prevflag = false
					$scope.prev.query = query;
				}
				else
				{
					$scope.prevflag = true
					
				}	
				
				if(end < $scope.total)
				{
					$scope.next.start = end;
					$scope.next.end = end + 10 < $scope.total ? end + 10 : $scope.total
					$scope.nextflag = false
					$scope.next.query = query;
				}		
				else
				{
					$scope.nextflag = true
				}
				
				console.log($scope.nextflag);
				console.log($scope.prevflag);			 
			});
	}
  }


  $scope.instasearchrequestrange = function(query, start, end){
	  
  	console.log("Suggestion selected: " + query);
	
	if(query.length > 0)
	{
		
		if(query in $scope.results)
		{
				$scope.data = $scope.results[query].data;
			 	$scope.total = $scope.results[query].total;
			 	$scope.next = {start:0, end:-1, query:query}
  				$scope.prev = {start:0, end:-1, query:query}	
				
				if(start > 0)
				{
					$scope.prev.end = start;
					$scope.prev.start = start - 10 > 0 ? start - 10 : 0;
					$scope.prevflag = false
					$scope.prev.query = query;
				}
				else
				{
					$scope.prevflag = true
					
				}	
				
				if(end < $scope.total)
				{
					$scope.next.start = end;
					$scope.next.end = end + 10 < $scope.total ? end + 10 : $scope.total
					$scope.nextflag = false
					$scope.next.query = query;
				}		
				else
				{
					$scope.nextflag = true
				}
				
				console.log("cache");	
		}
		else
    	$http.get("http://localhost:25809/search?query="+query+"&ranker=comprehensive&start="+start+"&end="+end).success(
		function(response) 
			{
				$scope.results[query] = response;
				$scope.data = response.data;
			 	$scope.total = response.total;
			 	$scope.next = {start:0, end:-1, qu:query}
  				$scope.prev = {start:0, end:-1, qu:query}	
				
				if(start > 0)
				{
					$scope.prev.end = start;
					$scope.prev.start = start - 10 > 0 ? start - 10 : 0;
					$scope.prevflag = false
					$scope.prev.query = query;
				}
				else
				{
					$scope.prevflag = true
					
				}	
				
				if(end < $scope.total)
				{
					$scope.next.start = end;
					$scope.next.end = end + 10 < $scope.total ? end + 10 : $scope.total
					$scope.nextflag = false
					$scope.next.query = query;
				}		
				else
				{
					$scope.nextflag = true
				}
				console.log("no-cache")			 
			});
	}
  }

});
