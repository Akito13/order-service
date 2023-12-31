package com.example.bookshop.orderservice.exception;

import com.example.bookshop.orderservice.dto.OrderDto;
import com.example.bookshop.orderservice.dto.ResponseDto;
import com.example.bookshop.orderservice.mapper.CommonMapper;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.CardException;
import com.stripe.exception.RateLimitException;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleOrderNotFoundException(OrderNotFoundException exception, WebRequest request){
        return new ResponseEntity<>(CommonMapper.buildErrorResponse(exception, request, null, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidBodyException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidBodyException(InvalidBodyException exception, WebRequest request){
        return new ResponseEntity<>(CommonMapper.buildErrorResponse(exception, request, null, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CannotCancelOrder.class)
    public ResponseEntity<ErrorResponseDto> handleCannotCancelOrder(CannotCancelOrder exception, WebRequest request){
        return new ResponseEntity<>(CommonMapper.buildErrorResponse(exception, request, null, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SachNotAvailableException.class)
    public ResponseEntity<ErrorResponseDto> handleAccountNotFoundException(SachNotAvailableException exception, WebRequest request){
        return new ResponseEntity<>(CommonMapper.buildErrorResponse(exception, request, null, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CardException.class)
    public ResponseEntity<ErrorResponseDto> handleCardException(CardException exception, WebRequest request){
        return new ResponseEntity<>(CommonMapper.buildErrorResponse(new CardException("Không thể xác minh thẻ", exception.getRequestId(), exception.getCode(), exception.getParam(), exception.getDeclineCode(), exception.getCharge(), exception.getStatusCode(), exception), request, null, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponseDto> handleRateLimitException(RateLimitException exception, WebRequest request){
        return new ResponseEntity<>(CommonMapper.buildErrorResponse(exception, request, null, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApiConnectionException.class)
    public ResponseEntity<ErrorResponseDto> handleApiConnectionException(ApiConnectionException exception, WebRequest request){
        return new ResponseEntity<>(CommonMapper.buildErrorResponse(new ApiConnectionException("Kết nối bị gián đoạn"), request, null, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(StripeException.class)
    public ResponseEntity<ErrorResponseDto> handleStripeException(StripeException exception, WebRequest request){
        return new ResponseEntity<>(CommonMapper.buildErrorResponse(new StripeException("Cổng thanh toán gặp lỗi", exception.getRequestId(), exception.getCode(), exception.getStatusCode()) {}, request, null, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(objectError -> {
            String fieldName = ((FieldError) objectError).getField();
            String message = objectError.getDefaultMessage();
            errors.put(fieldName, message);
        });
        ErrorResponseDto errorResponseDto = ErrorResponseDto.builder()
                .apiPath(request.getDescription(false))
                .message("Thông tin nhập không hợp lệ")
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST)
                .errors(errors)
                .build();
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<OrderDto>> handleGlobalException(Exception exception, WebRequest request){
        ResponseDto<OrderDto> responseDto = ResponseDto.<OrderDto>builder()
                .apiPath(request.getDescription(false))
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                .payload(null)
                .build();
        exception.printStackTrace();
        return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
