$('document').ready(function () { 
 data = {
		"nodes": [{
			"serialNo": 0,
			"instanceState": "running",
			"name": "10.152.177.185",
			"instanceId": "i-738fd609",
			"publicDNS": "ec2-54-237-74-58.compute-1.amazonaws.com",
			"isStartPoint": false
		},
		{
			"serialNo": 1,
			"instanceState": "running",
			"name": "10.178.8.73",
			"instanceId": "i-1ea55272",
			"publicDNS": "ec2-50-19-178-220.compute-1.amazonaws.com",
			"isStartPoint": false
		},
		{
			"serialNo": 2,
			"instanceState": "running",
			"name": "10.164.104.44",
			"instanceId": "i-45be123f",
			"publicDNS": "ec2-54-221-33-137.compute-1.amazonaws.com",
			"isStartPoint": false
		}],
		"links": [{
			"relationship": "rel",
			"source": 0,
			"target": 1
		},
		{
			"relationship": "rel",
			"source": 0,
			"target": 2
		},
		{
			"relationship": "rel",
			"source": 1,
			"target": 0
		},
		{
			"relationship": "rel",
			"source": 1,
			"target": 2
		},
		{
			"relationship": "rel",
			"source": 2,
			"target": 0
		},
		{
			"relationship": "rel",
			"source": 2,
			"target": 1
		}]
	}; 
 });