import calendar
from datetime import date, datetime#, timedelta
import time
import requests
import numpy as np
import json
# from tkinter import *
# import math

api_key = "fa88f55815a1fec406c195a31ed90158"
lat = 51.177601
lon = 71.432999
startYear = 1606266000 # Wednesday, 25 November 2020 г., 1:00:00
today = calendar.timegm(time.strptime(str(date.today()), '%Y-%m-%d')) # always first day of the year

def hello():
    return "Hello World"

def get_airPollutionYear():
    api_key = "fa88f55815a1fec406c195a31ed90158"
    lat = 51.177601
    lon = 71.432999
    end = today
    start = startYear

    arrMeanDt = []
    arrMeanAQI = []
    arrMeanPM10 = []
    arrMeanPM25 = []
    arrMeanNO2 = []
    arrMeanO3 = []
    while start < end:
        count = 0
        if (start == 1606266000):
            end = 1609459199 # Thursday, 31 December 2020 г., 23:59:59
            url = f"http://api.openweathermap.org/data/2.5/air_pollution/history?lat={lat}&lon={lon}&start={start}&end={end}&appid={api_key}"
            response = requests.get(url).json()
            for i in range(start, end, (60 * 60)):
                count += 1

            arrPM10List = []
            arrNO2List = []
            arrO3List = []
            arrPM25List = []
            arrAqiList = []
            arrDtList = []
            for i in range(count):
                arrAqi = response['list'][i]['main']['aqi']
                arrAqiList.append(arrAqi)
                arrPM10 = response['list'][i]['components']['pm10']
                arrPM10List.append(arrPM10)
                arrNO2 = response['list'][i]['components']['no2']
                arrNO2List.append(arrNO2)
                arrPM25 = response['list'][i]['components']['pm2_5']
                arrPM25List.append(arrPM25)
                arrO3 = response['list'][i]['components']['o3']
                arrO3List.append(arrO3)
                arrDt = response['list'][i]['dt']
                arrDtList.append(arrDt)

            meanAQI = round(np.mean(arrAqiList))
            meanPM10 = round(np.mean(arrPM10List), 2)
            meanPM25 = round(np.mean(arrPM25List), 2)
            meanNO2 = round(np.mean(arrNO2List), 2)
            meanO3 = round(np.mean(arrO3List), 2)
            meanDt = round(np.mean(arrDtList))
            yearNumber = datetime.utcfromtimestamp(meanDt).strftime('%Y')

            arrMeanAQI.append(meanAQI)
            arrMeanDt.append(yearNumber)
            arrMeanPM10.append(meanPM10)
            arrMeanPM25.append(meanPM25)
            arrMeanNO2.append(meanNO2)
            arrMeanO3.append(meanO3)

            start = 1609459200 #Friday, 1 January 2021 г., 0:00:00

        if(start == 1609459200):
            end = start + (60 * 60 * 24 * 365) # plus one year
            url = f"http://api.openweathermap.org/data/2.5/air_pollution/history?lat={lat}&lon={lon}&start={start}&end={end}&appid={api_key}"
            response = requests.get(url).json()
            count = len(response['list'])

            arrPM10List = []
            arrNO2List = []
            arrO3List = []
            arrPM25List = []
            arrAqiList = []
            arrDtList = []
            for i in range(count):
                arrAqi = response['list'][i]['main']['aqi']
                arrAqiList.append(arrAqi)
                arrPM10 = response['list'][i]['components']['pm10']
                arrPM10List.append(arrPM10)
                arrNO2 = response['list'][i]['components']['no2']
                arrNO2List.append(arrNO2)
                arrPM25 = response['list'][i]['components']['pm2_5']
                arrPM25List.append(arrPM25)
                arrO3 = response['list'][i]['components']['o3']
                arrO3List.append(arrO3)
                arrDt = response['list'][i]['dt']
                arrDtList.append(arrDt)

            meanAQI = round(np.mean(arrAqiList))
            meanPM10 = round(np.mean(arrPM10List), 2)
            meanPM25 = round(np.mean(arrPM25List), 2)
            meanNO2 = round(np.mean(arrNO2List), 2)
            meanO3 = round(np.mean(arrO3List), 2)
            meanDt = round(np.mean(arrDtList))
            yearNumber = datetime.utcfromtimestamp(meanDt).strftime('%Y')

            start = end
            arrMeanAQI.append(meanAQI)
            arrMeanDt.append(yearNumber)
            arrMeanPM10.append(meanPM10)
            arrMeanPM25.append(meanPM25)
            arrMeanNO2.append(meanNO2)
            arrMeanO3.append(meanO3)

        if(start > end): # count mean if only the year ends
            break
    data = {
        "aqi": arrMeanAQI,
        "dt": arrMeanDt,
        "pm10": arrMeanPM10,
        "pm25": arrMeanPM25,
        "no2": arrMeanNO2,
        "o3": arrMeanO3
    }

    return data
    
    # with open('output.json', 'w') as outfile:
    #     json.dump(data, outfile)


def parseJson():
    with open('output.json', 'w') as outfile:
        json.dump(get_airPollution(), outfile)

def regionAQI(lat, lon):
    url = f"https://api.openweathermap.org/data/2.5/air_pollution?lat={lat}&lon={lon}&appid={api_key}"
    response = requests.get(url).json()
    aqi = response['list'][0]['main']['aqi']

    return aqi

# def regionAQI(lat, lon, region):
#     url = f"https://api.openweathermap.org/data/2.5/air_pollution?lat={lat}&lon={lon}&appid={api_key}"
#     response = requests.get(url).json()
#     aqi = response['list'][0]['main']['aqi']
#
#     return {
#         region: aqi
#     }


def get_airPollutionDay(lat, lon):
    url1 = f"https://api.openweathermap.org/data/2.5/air_pollution/forecast?lat={lat}&lon={lon}&appid={api_key}"
    response1 = requests.get(url1).json()
    end = response1['list'][-1]['dt']
    start = response1['list'][0]['dt']

    arrMeanDt = []
    arrMeanAQI = []


    while start < response1['list'][-1]['dt']:
        count = 0
        end = start + (60 * 60 * 24)
        url = f"http://api.openweathermap.org/data/2.5/air_pollution/history?lat={lat}&lon={lon}&start={start}&end={end}&appid={api_key}"
        response = requests.get(url).json()
        count = len(response['list'])

        arrAqiList = []
        arrDtList = []

        for i in range(count):
            arrAqi = response['list'][i]['main']['aqi']
            arrAqiList.append(arrAqi)
            arrDt = response['list'][i]['dt']
            arrDtList.append(arrDt)

        meanAQI = round(np.mean(arrAqiList))
        meanDt = round(np.mean(arrDtList))
        yearNumber = datetime.utcfromtimestamp(meanDt).strftime('%d %b, %a')

        arrMeanAQI.append(meanAQI)
        arrMeanDt.append(yearNumber)

        start = end

    data = {
        "aqi": arrMeanAQI,
        "dt": arrMeanDt
    }
    return data