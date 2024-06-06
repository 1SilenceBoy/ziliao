import time
import os
import requests
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler, FileSystemEvent
import win32api
import win32print


class PrintOnFileCreateHandler(FileSystemEventHandler):
    def __init__(self, watch_folder, file_extension='.prn'):
        self.watch_folder = watch_folder
        self.file_extension = file_extension

    def on_created(self, event):
        if not event.is_directory and event.src_path.endswith(self.file_extension):
            print(f"find file:{event.src_path}")
            # 获取默认的打印机
            printer_list = self.get_printer_list()
            # for printer in printer_list:
            #     print(printer)
            printer_name = win32print.GetDefaultPrinter()
            print(printer_name)
            self.print_file(event.src_path,printer_name)
            os.remove(event.src_path)  #完成后删除

    def get_printer_list(self):
        printers = win32print.EnumPrinters(win32print.PRINTER_ENUM_LOCAL | win32print.PRINTER_ENUM_CONNECTIONS)
        printer_list = []
        for printer in printers:
            printer_name = printer[2]
            printer_list.append(printer_name)
        return printer_list

    def print_file(self, file_path, printer_name):
        file_data = open(file_path, 'rb')
        data = file_data.read()
        file_data.close()
        # 打印文件
        hPrinter = win32print.OpenPrinter(printer_name)
        try:
            hJob = win32print.StartDocPrinter(hPrinter, 1, ("test_job", None, "RAW"))
            try:
                win32print.StartPagePrinter(hPrinter)
                print(data)
                win32print.WritePrinter(hPrinter, data)
                win32print.EndPagePrinter(hPrinter)
            finally:
                win32print.EndDocPrinter(hPrinter)
        finally:
            win32print.ClosePrinter(hPrinter)



event_handler = PrintOnFileCreateHandler(watch_folder=r'c:\test')
observer = Observer()
observer.schedule(event_handler, event_handler.watch_folder, recursive=False)

observer.start()
try:
    while True:
        time.sleep(1)
except KeyboardInterrupt:
    observer.stop()

observer.join()