import React from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  NativeModules,
  TouchableOpacity,
  requireNativeComponent,
  ScrollView
} from 'react-native';
const rnNative = NativeModules.ConnectNativeModule
const SimpleText = requireNativeComponent('SimpleText');
class HelloWorld extends React.Component {
  state = {appInfo: '点击读取当前 Flavor 配置'};

  render() {
    return (
      <ScrollView contentContainerStyle={styles.container}>
        <Text style={styles.title}>React Native 0.79.6</Text>
        <Text style={styles.subtitle}>共享 Native Module 演示</Text>
        <TouchableOpacity style={styles.button} onPress={() => rnNative?.showToast('Hello from RN')}>
          <Text style={styles.buttonText}>Native Toast</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => rnNative?.openWebView('https://example.com', 'RN 打开的 WebView')}>
          <Text style={styles.buttonText}>打开 WebView</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => rnNative?.shareText('来自 React Native 的分享', 'Smarola')}>
          <Text style={styles.buttonText}>系统分享</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={async () => {
          const info = await rnNative?.getAppInfo();
          this.setState({appInfo: JSON.stringify(info, null, 2)});
        }}>
          <Text style={styles.buttonText}>读取 App 配置</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => rnNative?.openNativePage('HOME')}>
          <Text style={styles.buttonText}>返回 Native 首页</Text>
        </TouchableOpacity>
        <Text style={styles.info}>{this.state.appInfo}</Text>
        <SimpleText
          style={{width: 300, height: 50, marginTop: 20}}
          text="Native UI Component"
          textColor="#FFFFFF"
          backgroundColor="#6750A4"
          textSize={18}
        />
      </ScrollView>
    );
  }
}
const styles = StyleSheet.create({
    container: {
      flex: 1,
      backgroundColor: '#fff',
      alignItems: 'center',
      justifyContent: 'center',
      paddingVertical: 36,
    },
    title: {fontSize: 27, fontWeight: '700', color: '#202028'},
    subtitle: {fontSize: 15, color: '#666', marginTop: 6, marginBottom: 20},
    button: {width: 300, padding: 14, backgroundColor: '#6750A4', borderRadius: 12, marginBottom: 10},
    buttonText: {color: '#fff', textAlign: 'center', fontSize: 16},
    info: {width: 300, padding: 14, backgroundColor: '#f2f0f7', borderRadius: 12, color: '#333'},
  });

AppRegistry.registerComponent(
  'MyReactNativeApp',
  () => HelloWorld
);
