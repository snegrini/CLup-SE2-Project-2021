import 'package:flutter/material.dart';
import 'package:store_app/views/result_page.dart';
import 'package:scan_preview/scan_preview_widget.dart';

/// Camera page to scan QRs
class ScanPage extends StatefulWidget {
  @override
  _ScanState createState() => _ScanState();
}

class _ScanState extends State<ScanPage> {
  String barcode = "";

  @override
  initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return SizedBox(
        width: double.infinity,
        height: double.infinity,
        child: ScanPreviewWidget(
          onScanResult: (result) {
            Navigator.pop(context);
            Navigator.push(
              context,
              MaterialPageRoute(builder: (context) => ResultPage(result)),
            );
          },
        ));
  }
}
