// fonction permettant de vérifier la saisie des champs obligatoires
function checkMandatoryFields(fieldsArray,message)
{

  var nbElements = fieldsArray.length;

  for (var l = 0; l < nbElements; l++)
  {
    var field = fieldsArray[l][0];
    var fieldLabel = fieldsArray[l][1];
    if (field.value ==  "" && !field.disabled){
      alert(message+" "+fieldLabel);
      field.focus();
      return false;
    }
  }
  return true;
}
// fonction permettant de vérifier si les valeurs de deux champs sont identiques
function areEquals(field1,field2,message)
{ 
  
  if (field1.value != field2.value){
    alert(message);
    field2.focus();
    return false;
  }
  return true;	
}

/*
 *
 */
