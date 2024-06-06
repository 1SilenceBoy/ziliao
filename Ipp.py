import os
import time
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler, FileSystemEvent
import IPPRequset


class PrintOnFileCreateHandler(FileSystemEventHandler):
    def __init__(self, printer_url, watch_folder, file_extension='.prn'):
        self.printer_url = printer_url
        self.watch_folder = watch_folder
        self.file_extension = file_extension

    def on_created(self, event):
        if not event.is_directory and event.src_path.endswith(self.file_extension):
            print(f"find file:{event.src_path}")
            if IPPRequset.create_get_printer_ipp(self.printer_url) == 200:
                print("打印机获取成功")
                file_path = event.src_path
                filename_with_ext = os.path.basename(event.src_path)
                # 分割文件名和扩展名
                filename, extension = os.path.splitext(filename_with_ext)
                username = input("请输入您的名字: \r\n")
                if IPPRequset.print_file(self.printer_url, file_path, filename.encode(), username.encode()) == 200:
                    print("文件打印成功,删除文件")
                    os.remove(event.src_path)  # 完成后删除
                else:
                    print("文件打印失败")
            else:
                print("打印机获取失败")


event_handler = PrintOnFileCreateHandler(printer_url=b'http://192.168.50.188:631/printers/OKI_LTD_C651_109',
                                         watch_folder=r'c:\test')
observer = Observer()
observer.schedule(event_handler, event_handler.watch_folder, recursive=False)
observer.start()
try:
    while True:
        time.sleep(1)
except KeyboardInterrupt:
    observer.stop()

observer.join()
