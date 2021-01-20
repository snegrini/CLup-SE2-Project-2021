import 'dart:io';
import 'dart:convert';
import 'package:crypto/crypto.dart';
import 'package:customer_app/util/api_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/util/token_manager.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:customer_app/views/home_page.dart';
import 'package:device_info/device_info.dart';

void main() => runApp(ClupApp());

class ClupApp extends StatefulWidget {
  _ClupAppState createState() => _ClupAppState();
}

class _ClupAppState extends State<ClupApp> {
  static const String _title = 'CLup App';
  Widget _homePage;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: _title,
      home: _homePage,
    );
  }

  @override
  void initState() {
    super.initState();
    _setLoadingBar();
    _getCustomerToken();
  }

  Future<void> _getCustomerToken() async {
    // Checking if a token is stored in the app
    var storage = new FlutterSecureStorage();
    String value = await storage.read(key: 'jwt');

    if (value == null) {
      // Token not found, retrieving a new one
      try {
        String token = await _requestCustomerToken();

        // Storing token and redirecting
        storage.write(key: 'jwt', value: token);
        TokenManager().token = token;

        setState(() {
          _homePage = HomePage();
        });
      } catch (e) {
        // Displaying the error message
        _setErrorPage(e);
      }
    } else {
      // Token already retrieved => redirect to the homepage
      TokenManager().token = value;

      setState(() {
        _homePage = HomePage();
      });
    }
  }

  Future<String> _getHashedDeviceId() async {
    // Gets the device ID to identify a user
    String identifier;
    final DeviceInfoPlugin deviceInfoPlugin = new DeviceInfoPlugin();
    try {
      if (Platform.isAndroid) {
        var build = await deviceInfoPlugin.androidInfo;
        identifier = build.androidId;
      } else if (Platform.isIOS) {
        var data = await deviceInfoPlugin.iosInfo;
        identifier = data.identifierForVendor;
      }
    } on PlatformException {
      return Future.error('Failed to get platform version');
    }

    // Hashing the ID for privacy and data consistency
    var bytes = utf8.encode(identifier);
    return sha256.convert(bytes).toString();
  }

  Future<String> _requestCustomerToken() async {
    String token;
    try {
      var deviceId = await _getHashedDeviceId();
      token = await ApiManager.customerTokenRequest(deviceId);
    } catch (err) {
      return Future.error(err);
    }

    return token;
  }

  void _setErrorPage(String text) {
    setState(() {
      _homePage = new Scaffold(
        backgroundColor: Colors.white,
        body: Center(child: Text(text)),
      );
    });
  }

  void _setLoadingBar() {
    setState(() {
      _homePage = new Scaffold(
          backgroundColor: Colors.white,
          body: Center(
              child: CircularProgressIndicator(
                  valueColor: new AlwaysStoppedAnimation<Color>(
                      ClupColors.grapefruit))));
    });
  }
}
