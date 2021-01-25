import 'dart:convert';
import 'dart:io';

import 'package:crypto/crypto.dart';
import 'package:device_info/device_info.dart';
import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'api_manager.dart';

class AuthManager {
  var storage;

  AuthManager() {
    storage = new FlutterSecureStorage();
  }

  /// Gets the token from the secure storage (Keychain/Keystore)
  Future<String> getAuthToken() async {
    return await storage.read(key: 'jwt');
  }

  /// Writes the auth token in the secure storage
  void writeAuthToken(String token) {
    storage.write(key: 'jwt', value: token);
  }

  /// Returns true if the token is set in the secure storage, false otherwise
  Future<bool> isAuthTokenSet() async {
    return (await storage.read(key: 'jwt')) == null ? false : true;
  }

  /// Gets the device ID and returns his SHA256 hash.
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

  /// Performs the requests of a customer token to the server.
  Future<String> requestCustomerToken() async {
    String token;
    try {
      var deviceId = await _getHashedDeviceId();
      token = await ApiManager.customerTokenRequest(deviceId);
    } catch (err) {
      return Future.error(err);
    }

    return token;
  }
}
