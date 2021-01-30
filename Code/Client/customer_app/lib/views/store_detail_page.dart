import 'package:customer_app/model/store.dart';
import 'package:customer_app/model/ticket.dart';
import 'package:customer_app/util/api_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/util/data_manager.dart';
import 'package:customer_app/views/ticket_detail_page.dart';
import 'package:flutter/material.dart';

class StoreDetailPage extends StatefulWidget {
  final int storeId;

  const StoreDetailPage(this.storeId);

  _StoreDetailState createState() => _StoreDetailState();
}

class _StoreDetailState extends State<StoreDetailPage> {
  Store _store;
  bool _loading = true;
  bool _disabled = false;
  bool _error = false;
  String _errorText = "";

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text('Store'),
          backgroundColor: ClupColors.grapefruit,
        ),
        body: _loading
            ? Center(
                child: CircularProgressIndicator(
                    valueColor: new AlwaysStoppedAnimation<Color>(
                        ClupColors.grapefruit)))
            : Center(
                child: Column(
                  children: [
                    Text(_store.name),
                    RaisedButton(
                      onPressed: _disabled ? null : _addTicket,
                      child: Text(
                        'Retrieve a ticket',
                        style: TextStyle(color: Colors.white),
                      ),
                      color: ClupColors.grapefruit,
                      disabledColor: ClupColors.disabledGrapefruit,
                    ),
                    if (_error) Text(_errorText)
                  ],
                ),
              ));
  }

  @override
  void initState() {
    super.initState();
    _storeDetailRequest();
  }

  Future<void> _storeDetailRequest() async {
    try {
      var storeJson = await ApiManager.storeDetailRequest(
          DataManager().token, widget.storeId);

      setState(() {
        _loading = false;
        _error = false;
        _store = Store.fromJson(storeJson);
      });
    } catch (e) {
      setState(() {
        _loading = false;
        _error = true;
        _errorText = e;
      });
    }
  }

  Future<void> _addTicket() async {
    setState(() {
      _disabled = true;
    });

    try {
      var storeJson = await ApiManager.addTicketRequest(
          DataManager().token, widget.storeId);

      Ticket ticket = Ticket.fromJson(storeJson);
      setState(() {
        _error = false;
        _disabled = false;
      });

      _redirectToTicketDetail(ticket);
    } catch (e) {
      setState(() {
        _error = true;
        _disabled = false;
        _errorText = e;
      });
    }
  }

  void _redirectToTicketDetail(Ticket ticket) {
    Navigator.push(
      context,
      MaterialPageRoute(
          builder: (context) => TicketDetailPage.fromTicket(ticket)),
    );
  }
}
