import asyncio
from bleak import BleakClient, BleakScanner
from datetime import datetime
import struct
import time


class PineTimeConnect:
    # PineTime/InfiniTime UUIDs
    CURRENT_TIME_SERVICE_UUID = "00001805-0000-1000-8000-00805f9b34fb"
    CURRENT_TIME_CHAR_UUID = "00002a2b-0000-1000-8000-00805f9b34fb"

    NOTIFICATION_SERVICE_UUID = "00001811-0000-1000-8000-00805f9b34fb"
    ALERT_NOTIFICATION_CHAR_UUID = "00002a46-0000-1000-8000-00805f9b34fb"

    BATTERY_SERVICE_UUID = "0000180f-0000-1000-8000-00805f9b34fb"
    BATTERY_LEVEL_CHAR_UUID = "00002a19-0000-1000-8000-00805f9b34fb"

    def __init__(self):
        self.client = None
        self.device = None

    async def scan_for_device(self):
        """Scan for InfiniTime device"""
        print("Scanning for InfiniTime...")
        devices = await BleakScanner.discover()
        for device in devices:
            if device.name and "InfiniTime" in device.name:
                self.device = device
                print(f"Found InfiniTime: {device.address}")
                return True
        return False

    async def connect(self):
        """Connect to InfiniTime"""
        if not self.device:
            found = await self.scan_for_device()
            if not found:
                print("InfiniTime not found!")
                return False

        print(f"Connecting to {self.device.address}...")
        self.client = BleakClient(self.device.address)
        await self.client.connect()
        print("Connected!")
        return True

    async def sync_time(self):
        """Sync current time to InfiniTime"""
        if not self.client:
            return False

        current_time = datetime.now()
        # Format time according to InfiniTime specifications
        time_bytes = struct.pack(
            '<HBBBBBBBB',
            current_time.year,
            current_time.month,
            current_time.day,
            current_time.hour,
            current_time.minute,
            current_time.second,
            current_time.weekday(),
            0,  # 250ths of a second
            0  # Timezone (0 = UTC)
        )

        try:
            await self.client.write_gatt_char(
                self.CURRENT_TIME_CHAR_UUID,
                time_bytes
            )
            print("Time synced successfully")
            return True
        except Exception as e:
            print(f"Error syncing time: {e}")
            return False

    async def send_notification(self, title, message):
        """Send notification to InfiniTime"""
        if not self.client:
            return False

        # Format notification according to InfiniTime protocol
        notification = bytes([len(title)]) + title.encode() + b'\x00' + message.encode()

        try:
            await self.client.write_gatt_char(
                self.ALERT_NOTIFICATION_CHAR_UUID,
                notification
            )
            print("Notification sent successfully")
            return True
        except Exception as e:
            print(f"Error sending notification: {e}")
            return False

    async def get_battery_level(self):
        """Get battery level from InfiniTime"""
        if not self.client:
            return None

        try:
            battery_level = await self.client.read_gatt_char(
                self.BATTERY_LEVEL_CHAR_UUID
            )
            level = int(battery_level[0])
            print(f"Battery level: {level}%")
            return level
        except Exception as e:
            print(f"Error reading battery level: {e}")
            return None

    async def disconnect(self):
        """Disconnect from InfiniTime"""
        if self.client:
            await self.client.disconnect()
            print("Disconnected from InfiniTime")


# Simple test script
async def main():
    watch = PineTimeConnect()

    try:
        # Connect to device
        connected = await watch.connect()
        if not connected:
            return

        # Sync time
        await watch.sync_time()

        # Send test notification
        await watch.send_notification(
            "Test",
            "Hello from Python!"
        )

        # Get battery level
        await watch.get_battery_level()

        # Wait a bit before disconnecting
        await asyncio.sleep(2)

    except Exception as e:
        print(f"Error occurred: {e}")

    finally:
        # Always try to disconnect properly
        await watch.disconnect()


if __name__ == "__main__":
    asyncio.run(main())
