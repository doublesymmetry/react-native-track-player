# NotificationClickBehavior

Configuration for notification click behavior on Android.

| Param | Type  | Default | Description |
|-------|-------|---------|-------------|
| `enabled` | `boolean` | `true` | Whether to add URI data to the notification click intent. When disabled, the app will launch without any URI information. |
| `customUri` | `string` | `"trackplayer://notification.click"` | Custom URI to use instead of the default. Only used when `enabled` is `true`. |
| `action` | `string` | `"android.intent.action.VIEW"` | Custom action to use for the notification click intent. Only used when `enabled` is `true`. |

## Default Value

```typescript
{
  enabled: true,
  customUri: "trackplayer://notification.click",
  action: "android.intent.action.VIEW"
}
```

## Examples

### Disable URI data (launch app without notification click detection)
```typescript
await TrackPlayer.updateOptions({
  android: {
    notificationClickBehavior: {
      enabled: false
    }
  }
});
```

### Use custom URI
```typescript
await TrackPlayer.updateOptions({
  android: {
    notificationClickBehavior: {
      enabled: true,
      customUri: "myapp://notification.click"
    }
  }
});
```

### Use custom action
```typescript
await TrackPlayer.updateOptions({
  android: {
    notificationClickBehavior: {
      enabled: true,
      action: "android.intent.action.MAIN"
    }
  }
});
```

### Complete configuration
```typescript
await TrackPlayer.updateOptions({
  android: {
    notificationClickBehavior: {
      enabled: true,
      customUri: "myapp://player.notification",
      action: "android.intent.action.VIEW"
    }
  }
});
``` 