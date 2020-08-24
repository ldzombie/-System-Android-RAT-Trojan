<?php 
$infojson = file_get_contents("php://input");
file_put_contents("Blocks.json",$infojson);
?>