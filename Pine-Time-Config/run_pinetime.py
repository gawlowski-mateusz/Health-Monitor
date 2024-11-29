# Create a file named run_pinetime.py
import asyncio
from pinetime_connect import PineTimeConnect


async def main():
    watch = PineTimeConnect()

    # Connect to PineTime
    connected = await watch.connect()
    if not connected:
        print("Could not connect to PineTime")
        return

    # Example operations
    await watch.sync_time()  # Sync the time

    # Send a test notification
    await watch.send_notification("Hello", "This is a test message!")

    # Read battery level
    battery = await watch.get_battery_level()
    if battery:
        print(f"Battery level: {battery}%")

    # Wait a bit before disconnecting
    await asyncio.sleep(2)

    # Disconnect
    await watch.disconnect()


# Run the script
if __name__ == "__main__":
    asyncio.run(main())
