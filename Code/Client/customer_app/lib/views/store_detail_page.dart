import 'dart:collection';
import 'dart:convert';

import 'package:customer_app/enum/DayOfWeek.dart';
import 'package:customer_app/model/opening_hour.dart';
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
  Map<String, List<OpeningHour>> _ohMap;


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
                    Text(
                      _store.name,
                      style:
                      TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
                    ),
                    Text(
                      _store.address.toString(),
                      style: TextStyle(color: Colors.grey, fontSize: 20)
                    ),
                    SizedBox(
                      height: 100.0,
                      width: 100.0,
                      child: Image.memory(base64Decode(_store.image))
                    ),
                    Row(
                      children: <Widget>[
                        Expanded(
                          child: Text('Estimated wait time', textAlign: TextAlign.center),
                        ),
                        Expanded(
                          child: Text(_store.estimateTime.toString() + " min", textAlign: TextAlign.center),
                        ),
                      ],
                    ),
                    Row(
                      children: <Widget>[
                        Expanded(
                          child: Text('People in queue', textAlign: TextAlign.center),
                        ),
                        Expanded(
                          child: Text(_store.customerInQueue.toString() + " persons", textAlign: TextAlign.center),
                        ),
                      ],
                    ),
                    Text(
                        "Opening hours",
                        style: TextStyle(color: Colors.grey, fontSize: 20)
                    ),

                    // List of opening hours
                    for (var ohEntry in _ohMap.entries) Row(children: <Widget>[
                      Expanded(
                        child: Text(ohEntry.key, textAlign: TextAlign.center),
                      ),
                      Expanded(
                        child: Column(
                          children : [
                            for (var oh in ohEntry.value) Row(children: <Widget>[
                              Text(oh.from.format(context) + " - " + oh.to.format(context), textAlign: TextAlign.center)
                            ])
                          ]
                        ),
                      ),
                    ]),

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
        _ohMap = _ohListToMap(_store.openingHours);
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

  Map<String, List<OpeningHour>> _ohListToMap(List<OpeningHour> ohList) {
    Map<String, List<OpeningHour>> openingHourMap = new LinkedHashMap();

    for (OpeningHour oh in ohList) {
      String dayName = DayOfWeekHelper.of(oh.weekDay).name;

      openingHourMap.putIfAbsent(dayName, () => List());
      openingHourMap[dayName].add(oh);
    }
    return openingHourMap;
  }
}
