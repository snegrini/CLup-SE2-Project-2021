import 'package:flutter/material.dart';
import 'package:store_app/util/clup_colors.dart';
import 'package:store_app/views/scan_page.dart';
import 'package:permission_handler/permission_handler.dart';

class QrButtonPage extends StatelessWidget {
  void _buttonPress(BuildContext context) async {
    if (await Permission.camera.request().isGranted) {
      Navigator.push(
        context,
        MaterialPageRoute(builder: (context) => ScanPage()),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text('CLup Store App'),
          backgroundColor: ClupColors.grapefruit,
        ),
        body: Center(
          child: new RaisedButton(
            child: new Text(
              'Scan QR',
              style: new TextStyle(color: Colors.white),
            ),
            onPressed: () => _buttonPress(context),
            color: ClupColors.grapefruit,
          ),
        ));
  }
}
