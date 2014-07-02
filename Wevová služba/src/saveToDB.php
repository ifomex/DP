<?php
	$localhost = "localhost";
	//zde je třeba zadat přihlašovací jméno a heslo k databázi
	$login = "xblatn03";
	$password = "guj7emco";  
	
	$db = mysql_connect('localhost:/var/run/mysql/mysql.sock', $login, $password);
	if (!$db) die('nelze se pripojit '.mysql_error());
    if (!mysql_select_db('xblatn03', $db)) die('database neni dostupna '.mysql_error());
	
	$foo = file_get_contents("php://input");
	
	$json = json_decode($foo, true);
	$jour_name = $json["jour_name"];
	$jour_sdate = $json["jour_sdate"];
	$jour_edate = $json["jour_edate"];
	$data_count = $json["j_count"];
	$data = $json["activities"];

	//vytvoření cesty v databázi
	//var_dump($data);
	mysql_query("SET NAMES 'UTF-8'");
	mysql_query("insert into journeys(name, sdate, edate) 
			values('". $jour_name ."','". $jour_sdate ."','". $jour_edate ."');",
			$db);
	$jour_id = mysql_insert_id();
	
	//vytvoření aktivit v databázi
	foreach($data as $row) {
		mysql_query("SET NAMES 'UTF-8'");
		mysql_query("insert into activities(name, stime, etime, pl_name, address, lat, lon, category, journey_id)
			values('" . $row["a_name"] . "',
				'" . $row["a_stime"] . "',
				'" . $row["a_etime"] . "',
				'" . $row["pl_name"] . "',
				'" . $row["address"] . "',
				'" . $row["a_lat"] . "',
				'" . $row["a_lon"] . "',
				'" . $row["category"] . "',
				'" . $jour_id . "');"
			, $db);
	}	
	
	//návrat identifikátoru cesty
	$res = array();
	$res["jid"] = $jour_id;
	
	echo json_encode($res);
?>