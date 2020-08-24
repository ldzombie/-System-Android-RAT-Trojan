<?php 
	require "db.php";
	require "./libs/xcopy.php";
	$data = $_GET;

	$dir_log = './Devices/'.($data['deviceid']).'/fg_log.txt';
	$dir_new = './Devices/'.($data['deviceid']);

	$deviceD = R::findOne('devices','device_id=?',array($data['deviceid']));
	if($deviceD!=null){
		if(!is_dir('./Devices/'.$data['deviceid'])){
			mkdir("./Devices/".$data['deviceid']);
			cop($dir_new,$dir_log);
		}
		echo 'ok';
	}else{
		$device = R::dispense('devices');
		$device->name="";
		$device->deviceID=$data['deviceid'];
		$device->admin=$data['admin'];
		$device->android=$data['android'];
		$device->userID=$data['userid'];
		$device->add_time=$data['time'];
		$device->app_version=$data['appV'];
		R::store($device);
		
		if(!is_dir('./Devices/'.$data['deviceid'])){
			mkdir($dir_new);
			cop($dir_new,$dir_log);
			echo 'create';
		}
		
		$user = R::findOne('users','sid=?',array($data['userid']));
		$user->devices = $user->devices+1;

		R::store($user);
	}

	function cop($d2,$dl){
		$dir='./Devices/Default';
		copy_folder($dir,$d2,$dl);
	}
?>	