<?php 
	require "db.php";
	unset($_SESSION['logged_used']);
	
	if($_SESSION['admin']==true)
		unset($_SESSION['admin']);
	unset($_SESSION['device_select']);
	header('Location: /');
?>