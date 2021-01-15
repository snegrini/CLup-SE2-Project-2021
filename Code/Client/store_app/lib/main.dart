import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:store_app/views/login_page.dart';
import 'package:store_app/views/qr_button_page.dart';

void main() => runApp(ClupApp());

class ClupApp extends StatefulWidget {
  _ClupAppState createState() => _ClupAppState();
}

class _ClupAppState extends State<ClupApp> {
  static const String _title = 'CLup Store App';

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

    setState(() {
      _homePage = new Scaffold(
        backgroundColor: Colors.white,
      );
    });

    var storage = new FlutterSecureStorage();
    storage.read(key: 'jwt').then((String value) {
      if (value == null) {
        setState(() {
          _homePage = LoginPage();
        });
      } else {
        setState(() {
          _homePage = QrButtonPage();
        });
      }
    });
  }
}
