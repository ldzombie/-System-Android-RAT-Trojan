function usermenu(){
	var us = document.getElementById('usermenu');
	var dat = us.dataset;
	if(dat.open=="false"){
		dat.open = true;
	}
	else{
		dat.open = false;
	}
}
function closemenu(){
	var us = document.getElementById('usermenu');
	var dat = us.dataset;
	dat.open = false;
}
$(document).click(function (e) {
	if (!$(e.target).closest(".UserMenu,.usm").length) {
		var us = document.getElementById('usermenu');
		var dat = us.dataset;
		dat.open = false;
	}
});