import 'dart:async';

import 'package:flutter/services.dart';

class BarcodeScanner {
  static const CameraAccessDenied = 'PERMISSION_NOT_GRANTED';
  static const MethodChannel _channel =
      const MethodChannel('com.apptreesoftware.barcode_scan');

  static Future<String> scan({
    String flashOnTitle = 'Flash On',
    String flashOffTitle = 'Flash Off',
    String backTitle = 'Back',
    String fontName,
  }) async {
    return await _channel.invokeMethod('scan', {
      'flashOnTitle': flashOnTitle,
      'flashOffTitle': flashOffTitle,
      'backTitle': backTitle,
      'fontName': fontName,
    });
  }
}
