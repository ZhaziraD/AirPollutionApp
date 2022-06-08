import numpy as np
import cv2
import matplotlib.pyplot as plt
from PIL import Image
import io
import base64

def testHello():
	return "Hello World"

def main(X, Y):
	fig = plt.figure()

	x = X.split(",")
	y = Y.split(",")

	x_data = []
	y_data = []

	for i in x:
		x_data.append(np.double(i))
	for i in y:
		y_data.append(np.double(i))

	ay = fig.add_subplot(1, 1, 1)
	ay.plot(x_data, y_data)

	fig.canvas.draw()
	img = np.fromstring(fig.canvas.tostring_rgb(), np.uint8)#np.uint8, '')
	# img = np.fromstring(fig.canvas.tostring_rgb())#, dtype=np.unit8, sep='')
	img = img.reshape(fig.canvas.get_width_height() [::-1]+(3,))
	# img = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)

	pil_im = Image.fromarray(img)
	buff = io.BytesIO()

	pil_im.convert('RGB').save(buff, "PNG")
	# pil_im.save('newimage2.png')
	img_str = base64.b64encode(buff.getvalue())

	return "" + str(img_str, 'utf-8')


# def main(X, Y):
# 	fig = plt.figure()
#
# 	# x = X.split(",")
# 	# y = Y.split(",")
#
# 	x_data = []
# 	y_data = []
#
# 	for i in x:
# 		x_data.append(np.double(i))
# 	for i in y:
# 		y_data.append(np.double(i))
#
# 	ay = fig.add_subplot(1, 1, 1)
# 	ay.plot(x_data, y_data)
#
# 	fig.canvas.draw()
# 	img = np.fromstring(fig.canvas.tostring_rgb(), np.uint8)#np.uint8, '')
# 	# img = np.fromstring(fig.canvas.tostring_rgb())#, dtype=np.unit8, sep='')
# 	img = img.reshape(fig.canvas.get_width_height() [::-1]+(3,))
# 	# img = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)
#
# 	pil_im = Image.fromarray(img)
# 	buff = io.BytesIO()
#
# 	pil_im.convert('RGB').save(buff, "PNG")
# 	# pil_im.save('newimage2.png')
# 	img_str = base64.b64encode(buff.getvalue())
#
# 	return "" + str(img_str, 'utf-8')

# def main(n1, n2):
# 	# num1 = int(n1)
# 	# num2 = int(n2)
# 	#
# 	# sum = num1 + num2
#
# 	out = np.ones(5)
#
# 	return "zeros array" + str(out)