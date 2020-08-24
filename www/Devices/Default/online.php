<?php

$timestamp = file_get_contents("php://input");
file_put_contents("lastonline.txt", gmdate("d-m-Y\ H:i:s", $timestamp));
?>

