package main

import (
	"encoding/json"
	"math/rand"
	"time"

	producer "github.com/a8m/kinesis-producer"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/endpoints"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/kinesis"
)

type droneData struct {
	Id            int     `json:"id"`
	Longitude     float32 `json:"longitude"`
	Latitude      float32 `json:"latitude"`
	Altitude      float32 `json:"altitude"`
	Timestamp     string  `json:"timestamp"`
	BatteryLevel  int     `json:"battery_level"`
	Humidity      float32 `json:"humidity"`
	LightLevel    float32 `json:"light_level"`
	IsViolation   bool    `json:"is_violation"`
	ViolationCode int     `json:"violation_code"`
	ImageId       string  `json:"image_id"`
	CarMaker      string  `json:"car_maker"`
}

func randomInt(min, max int) int {
	return min + rand.Intn(max-min)
}

func main() {
	pr := producer.New(&producer.Config{
		StreamName: "spark-drone-message-stream",
		Client: kinesis.New(session.Must(session.NewSession(&aws.Config{
			Region: aws.String(endpoints.EuWest3RegionID),
		}))),
	})

	pr.Start()
	defer pr.Stop()

	id := randomInt(1, 101)
	counter := 0

	go func() {
		for r := range pr.NotifyFailures() {
			println("Fail: %v", r)
		}
	}()

	for {
		longitude := rand.Float32()*360 - 180
		latitude := rand.Float32()*360 - 180
		altitude := rand.Float32()*360 - 180
		timestamp := time.Now().Format("2006-01-02T15:04:05.000Z")
		battery := 100 - counter
		humidity := rand.Float32()
		lightLevel := rand.Float32()
		isViolation := randomInt(1, 101) <= 5
		violationCode := 101
		imageId := ""
		carMaker := ""

		if isViolation {
			violationCode = randomInt(0, 101)
		}

		message := &droneData{
			id,
			longitude,
			latitude,
			altitude,
			timestamp,
			battery,
			humidity,
			lightLevel,
			isViolation,
			violationCode,
			imageId,
			carMaker,
		}

		msg, err := json.Marshal(message)
		if err != nil {
			println("Error serialising: ", err.Error())
		}
		println(string(msg))

		err = pr.Put(msg, "id")
		if err != nil {
			println("Error sending message: ", err.Error())
		}

		time.Sleep(time.Second)
		counter += 1
	}
}
