import {Alert,NativeModules} from 'react-native';
import Contacts from 'react-native-contacts';

var phoneNumber = '';

const openContact = () =>{
	var newPerson = {
	  phoneNumbers: [{
		label: 'mobile',
		number: phoneNumber,
	  }],
	}

	Contacts.openContactForm(newPerson, (err, contact) => {
	  if (err) throw err;
	  // contact has been saved
	})
}
module.exports = async (taskData) => {
	if (taskData.state === 'extra_state_idle') {
		console.log("idle_rec");
		phoneNumber = '+34' + taskData.number;
		setTimeout(() => Alert.alert(
		  "Llamada finalizada",
		  "La llamada ha finalizado.\n¿Desea registrar la información?",
		  [
			{
			  text: "No",
			  onPress: () => console.log("Cancel Pressed"),
			  style: "cancel"
			},
			{ text: "Si", onPress: () => NativeModules.CallServiceManager.startFormActivity(phoneNumber) }
		  ],
		  { cancelable: false }
		),
		1000);
	}	
}