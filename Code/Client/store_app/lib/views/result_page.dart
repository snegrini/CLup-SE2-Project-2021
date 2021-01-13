import 'package:flutter/material.dart';
import 'package:store_app/util/clup_colors.dart';
import 'package:store_app/views/scan_page.dart';
import 'package:permission_handler/permission_handler.dart';

class ResultPage extends StatelessWidget {
  final String qrdata;

  ResultPage({Key key, @required this.qrdata}) : super(key: key);

  void _buttonPress(BuildContext context) async {
    if (await Permission.camera.request().isGranted) {
      Navigator.pop(context);
      Navigator.push(
        context,
        MaterialPageRoute(builder: (context) => ScanPage()),
      );
    }
  }

  Widget _getMessage() {
    if (qrdata == 'Minecraft') {
      return new Text(
        'Sei un vero Minecraftiano <3',
        style: new TextStyle(color: Colors.green, fontSize: 25),
      );
    } else {
      return new Text(
        'Non sei un Minecraftiano... Arghh',
        style: new TextStyle(color: Colors.red, fontSize: 25),
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
        body: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Padding(
                padding: EdgeInsets.all(16.0),
                child: _getMessage(),
              ),
              Center(
                child: new RaisedButton(
                  child: new Text(
                    'Scan new QR',
                    style: new TextStyle(color: Colors.white),
                  ),
                  onPressed: () => _buttonPress(context),
                  color: ClupColors.grapefruit,
                ),
              ),
            ]));
  }
}
