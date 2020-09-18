import React, {useState, useEffect} from 'react';
import {
  StyleSheet,
  View,
  Text,
  Button,
  TouchableHighlight,
  PermissionsAndroid,
  NativeModules,
} from 'react-native';

import AsyncStorage from '@react-native-community/async-storage';

const App: () => React$Node = () => {
  const [isCallServiceActive, setIsCallServiceActive] = useState(false);

  getIsCallServiceActive = async () => {
    try {
      const jsonValue = await AsyncStorage.getItem('@isCallServiceActive');
      if (jsonValue != null) {
        const wasActive = JSON.parse(jsonValue);
        setIsCallServiceActive(wasActive);
        wasActive ? NativeModules.CallServiceManager.startCallService() : NativeModules.CallServiceManager.stopCallService();
      }
    } catch (e) {
      alert('Failed to fetch the data from storage');
      console.log(e);
    }
  };

  saveIsCallServiceActive = async (value) => {
    try {
      console.log('Saving ' + value);
      await AsyncStorage.setItem('@isCallServiceActive', JSON.stringify(value));
    } catch (e) {
      alert('Failed to save the data to the storage');
      console.log(e);
    }
  };

  useEffect(() => {
    getIsCallServiceActive();
  }, []);
  return (
    <>
      <View style={styles.body}>
        <Text style={styles.text}>Activar detector de llamadas?</Text>
        <TouchableHighlight
          onPress={() => {
            if (isCallServiceActive) {
              NativeModules.CallServiceManager.stopCallService();
              setIsCallServiceActive(false);
              saveIsCallServiceActive(false);
			  NativeModules.CallServiceManager.saveIsCallServiceActive(false);
            } else {
              NativeModules.CallServiceManager.startCallService();
              setIsCallServiceActive(true);
              saveIsCallServiceActive(true);
			  NativeModules.CallServiceManager.saveIsCallServiceActive(true);
            }
          }}>
          <View
            style={{
              width: 200,
              height: 200,
              justifyContent: 'center',
              alignItems: 'center',
              backgroundColor: isCallServiceActive ? 'greenyellow' : 'red',
            }}>
            <Text style={styles.text}>
              {isCallServiceActive ? `ON` : `OFF`}{' '}
            </Text>
          </View>
        </TouchableHighlight>
      </View>
    </>
  );
};

const styles = StyleSheet.create({
  body: {
    backgroundColor: 'honeydew',
    justifyContent: 'center',
    alignItems: 'center',
    flex: 1,
  },
  text: {
    padding: 20,
    fontSize: 20,
  },
});

export default App;
