(function (_, $) {
    var xfaUtil = xfalib.ut.XfaUtil.prototype;
    $.widget('xfaWidget.sliderWidget', $.xfaWidget.abstractWidget, {
        _widgetName: 'sliderWidget',
        _superPrototype : $.xfaWidget.abstractWidget.prototype,
        _onSliderChange :function(slider,percent,event){
            var slider_val = slider.val();
            percent.text(slider_val + '%');

            // Build up selector for this instance of the widget
            var id = "#" + slider.context.id + ' > div > div';
            var stage1 = id + ".stage1";
            var stage2 = id + ".stage2";
            var stage3 = id + ".stage3";
            var stage4 = id + ".stage4";

            // Hide all stage divs
            $( stage1 ).addClass("hidden");
            $( stage2 ).addClass("hidden");
            $( stage3 ).addClass("hidden");
            $( stage4 ).addClass("hidden");

            // Make the relevant stage div visible
            switch (true){
                case slider_val > 0 && slider_val < 25:
                    $(stage1).removeClass("hidden");
                    break;
                case slider_val >= 25 && slider_val < 50:
                    $(stage2).removeClass("hidden");
                    break;
                case slider_val >= 50 && slider_val < 75:
                    $(stage3).removeClass("hidden");
                    break;
                case slider_val >= 75:
                    $(stage4).removeClass("hidden");
                    break;
            }

        },
        render : function()
        {
            var control = $.xfaWidget.abstractWidget.prototype.render.apply(this,arguments);
            var sliderInput = this.element.find('.slider-input');
            var percent = this.element.find('.slider-percent');

            sliderInput.on("input",$.proxy(this._onSliderChange, null, sliderInput,percent));


            return sliderInput;
        },

        /*
         * For reflecting the model changes the widget can register for the listeners in the getOptionsMap function.
         * For adding a listener ensure that instead of starting from scratch, get the option map from the super class.
         * The function returns a mapping which provides detail for what action to perform on change of an option. The
         * keys are the options that are provided to the widget and values are the function that should be called
         * whenever a change in that option is detected. The abstract Widget provides handlers for all the global
         * options (except value and displayValue). The various options include:
         * - tabIndex
         * - role
         * - screenReaderText
         * - paraStyles
         * - dir
         * - height
         * - width
         * - isValid
         * - access
         * - value
         * - displayValue
         * - placeholder
         * - items [ListBox, DropDownList]
         * - maxChars [TextField]
         * - multiLine [TextField]
         * - svgCaption [Button]
         * - allowNeutral [CheckBox]
         * You only need to override the options which require a behavior change (as compared to default Out-of-the-box
         * widget).
         */
        getOptionsMap: function(){
            console.log('getOptionsMap');
            var parentOptionsMap = $.xfaWidget.abstractWidget.prototype.getOptionsMap.apply(this,arguments),
                newMap = $.extend({},parentOptionsMap,
                    {
                        "value" : function (value) {
                            if(value === null){
                                value = 0;
                            }
                            this.$userControl.val(value);
                            this.$userControl.trigger("input");
                        }
                    });
            return newMap;
        },

        /*
         * To enable the widget to throw XFA events that, we need to modify the getEventMap function. The function
         * returns a mapping of html events to the XFA events. The code below provides a mapping which essentially tells
         * the XFA framework that whenever html triggers focus on the datepicker element (the $userControl element)
         * execute the XFA enter event and run any script written on that event for this field.
         * In case the custom widget requires to throw an HTML blur / change event on non-default actions as well, then
         * a custom event may be dispatched, and the custom event may be mapped with the XFA event below.
         */
        getEventMap: function() {
            var parentEventMap = $.xfaWidget.abstractWidget.prototype.getEventMap.apply(this,arguments),
                newMap = $.extend({},parentEventMap,
                    {
                        blur: "xfaexit"
                        //...
                    });
            return newMap;
        },

        /*
         * XFA provides display and edit picture clause which determines the format in which the value will be visible
         * in the UI and the input format in which the user will enter the value. The value the user enters into the
         * field is formatted after the user exits from the field and similarly when they enter the field, edit value is
         * displayed to them (As per mobile forms limitation, instead of edit Value, rawValue is displayed).

         * The framework accomplishes this by providing two values to the widget, displayValue and value. The framework
         * also calls the showDisplayValue function after the Exit event and showValue function before the Enter event
         * and it is the responsibility of the widget to implement how it wants these values to appear to the user.
         */
        showDisplayValue: function() {
            // this.$userControl.val(this.options.displayValue)
        },

        showValue: function() {
            // this.$userControl.val(this.options.value)
        },

        /*
         * According to the specification the value returned by the getCommitValue function of the widget is set to be
         * the value of the field. The framework calls the getCommitValue function to get the value at appropriate event
         * (The event for most fields is exit event, except for dropdown where the event can be change/exit).
         */
        getCommitValue: function() {
            return this.$userControl.val();
        }


    });
})(_, jQuery);