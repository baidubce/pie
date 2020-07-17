// Copyright 2020 Baidu Inc. All rights reserved.
// Use of this source code is governed by the license
// that can be found in the LICENSE file.

/*
modification history
--------------------
2020/7/17, by xiashuai01@baidu.com, create
*/

package util

import (
	"crypto/sha256"
	"encoding/hex"
	"log"
)

func HashToken(username, password, time string) string {
	hash := sha256.New()
	hash.Write([]byte(username + password + time))
	bs := hash.Sum(nil)
	hexBs := hex.EncodeToString(bs)
	return hexBs
}

func ErrorCheck(e error) {
	if e != nil {
		log.Fatalf("encount an error: %v", e)
	}
}
