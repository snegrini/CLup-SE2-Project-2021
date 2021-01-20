import 'package:flutter/material.dart';
import 'package:store_app/util/api_manager.dart';
import 'package:store_app/util/clup_colors.dart';
import 'package:store_app/util/token_manager.dart';
import 'package:store_app/views/scan_page.dart';
import 'package:permission_handler/permission_handler.dart';

/// Page that prompts the result of a validation request
class ResultPage extends StatefulWidget {
  final String qrdata;

  const ResultPage(this.qrdata);

  @override
  _ResultState createState() => _ResultState();
}

class _ResultState extends State<ResultPage> {
  bool _loaded = false;
  bool _gotError = false;
  String _text = "";

  void _buttonPress(BuildContext context) async {
    if (await Permission.camera.request().isGranted) {
      Navigator.pop(context);
      Navigator.push(
        context,
        MaterialPageRoute(builder: (context) => ScanPage()),
      );
    }
  }

  Color _getTextColor() {
    if (!_loaded) {
      return Colors.black;
    } else if (_gotError) {
      return Colors.red;
    } else {
      return Colors.green;
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
                child: Text(
                  _text,
                  style: new TextStyle(color: _getTextColor(), fontSize: 25),
                ),
              ),
              if (_loaded)
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

  @override
  void initState() {
    super.initState();

    setState(() {
      _loaded = false;
      _text = "Processing...";
    });

    _validationRequest();
  }

  /// Performs a validation request with the QR read from the previous page
  Future<void> _validationRequest() async {
    try {
      String message =
          await ApiManager.validateRequest(TokenManager().token, widget.qrdata);
      setState(() {
        _gotError = false;
        _text = message;
        _loaded = true;
      });
    } catch (e) {
      setState(() {
        _gotError = true;
        _text = e;
        _loaded = true;
      });
    }
  }
}
