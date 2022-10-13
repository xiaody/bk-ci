package middleware

import (
	"disaptch-k8s-manager/pkg/config"
	"disaptch-k8s-manager/pkg/types"
	"github.com/gin-gonic/gin"
	"net/http"
)

func InitApiAuth() gin.HandlerFunc {

	return func(c *gin.Context) {
		// 判断是否是来自蓝盾的token
		token := c.GetHeader(config.Config.Dispatch.ApiToken.Key)
		if token != "" && token == config.Config.Dispatch.ApiToken.Value {
			c.Next()
			return
		}

		c.AbortWithStatus(http.StatusUnauthorized)
		c.JSON(http.StatusUnauthorized, &types.Result{
			Data:    nil,
			Status:  http.StatusUnauthorized,
			Message: "no auth",
		})
	}
}
