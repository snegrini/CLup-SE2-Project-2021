import 'package:flutter/material.dart';
import 'package:store_app/views/login_page.dart';

void main() => runApp(ClupApp());

class ClupApp extends StatelessWidget {
  static const String _title = 'CLup App';

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: _title,
      home: LoginPage(),
    );
  }
}
