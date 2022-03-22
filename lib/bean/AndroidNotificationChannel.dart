import 'cloud_push_service_enums.dart' show AndroidNotificationImportance;

///android 通知
class AndroidNotificationChannel {

  final String id;
  final String name;
  final String description;
  final AndroidNotificationImportance importance;


  const AndroidNotificationChannel(this.id, this.name, this.description,
      {this.importance = AndroidNotificationImportance.DEFAULT});

  Map<String, dynamic> toJson() {
    return {
      "id": id,
      "name": name,
      "description": description,
      "importance": importance.index + 1
    };
  }
}
