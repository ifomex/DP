<?php
	$localhost = "localhost";
	//zde je třeba zadat přihlašovací jméno a heslo k databázi
	$login = "xblatn03";
	$password = "guj7emco";
	
	$jid = $_GET['jid'];
	
	$db = mysql_connect('localhost:/var/run/mysql/mysql.sock', 'xblatn03', 'guj7emco');
	if (!$db) die('nelze se pripojit '.mysql_error());
    if (!mysql_select_db('xblatn03', $db)) die('database neni dostupna '.mysql_error());
	
	$res = mysql_query('select * from journeys where journey_id = ' . $jid, $db);
	$journey = mysql_fetch_array($res, MYSQL_ASSOC);
	$activities = array();
	$res = mysql_query('select * from activities where journey_id = '.$jid, $db);

	while($acti = mysql_fetch_array($res, MYSQL_ASSOC)){
		$activities[] = $acti;
	}

	header("Content-type: application/json");
	
	$data = array();
	$data = $journey;
	$data["activities"] = $activities;
	

	echo json_encode($data);
?>