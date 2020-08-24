<?php 
$infojson = file_get_contents("php://input");
file_put_contents("callLog.json",$infojson);
?>