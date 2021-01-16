import 'package:flutter/material.dart';

import 'package:qr_flutter/qr_flutter.dart';

class PassesPage extends StatefulWidget {
  @override
  _PassesState createState() => _PassesState();
}

class _PassesState extends State<PassesPage> {
  String barcode = "";

  @override
  initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return QrImage(
      data: "Minecraft",
      version: QrVersions.auto,
      size: 200.0,
    );
  }
}
