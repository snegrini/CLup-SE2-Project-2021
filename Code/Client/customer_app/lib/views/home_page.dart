import 'package:flutter/material.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/views/stores_page.dart';
import 'package:customer_app/views/passes_page.dart';

/// Home page of the app with a bottom navigation bar to switch from the store list
/// to the store passes list and vice versa.
class HomePage extends StatefulWidget {
  @override
  _HomeState createState() => _HomeState();
}

class _HomeState extends State<HomePage> {
  int _selectedIndex = 0;

  static List<Widget> _widgetOptions = <Widget>[StoresPage(), PassesPage()];

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('CLup'),
        backgroundColor: ClupColors.grapefruit,
      ),
      body: Center(
        child: _widgetOptions.elementAt(_selectedIndex),
      ),
      bottomNavigationBar: BottomNavigationBar(
        items: const <BottomNavigationBarItem>[
          BottomNavigationBarItem(
            icon: Icon(Icons.list),
            label: 'List',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.confirmation_number),
            label: 'My Passes',
          ),
        ],
        currentIndex: _selectedIndex,
        selectedItemColor: ClupColors.grapefruit,
        onTap: _onItemTapped,
      ),
    );
  }
}
