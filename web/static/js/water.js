function showDescription(id){
div = gid("description_form");
div.style.visibility="visible";
div2 = gid("id");
div2.value = id;
var url = 't112818_MineralWater/waterService?id='+id;


$.getJSON(url, function (data) {
	$("#desc").val(data.content);
   
    
});

}

function hide_description_form(){
div = gid("description_form");
div.style.visibility="hidden";
}
function gid(id){
return document.getElementById(id);
}