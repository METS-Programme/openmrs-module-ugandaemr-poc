<div class="modal fade bd-order-modal-lg" id="add-order-to-lab-worklist-dialog" tabindex="-1" role="dialog"
     aria-labelledby="scheduleOrderModalLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <form id="addtesttoworklist">
            <div class="modal-content">
                <div class="modal-header">
                    <h3>${ui.message("SCHEDULE TEST")}</h3>
                </div>
                <div class="modal-body">

                    <span id="add_to_queue-container">
                        <input type="hidden" id="order_id" name="order_id" value="">
                    </span>

                    <div class="container">
                        <div class="row">
                            <div class="col-6">
                                <div class="form-group" id="specimen-id-container">
                                    <label for="sample_id">
                                        <span>${ui.message("SPECIMEN ID/SAMPLE ID")}</span>
                                    </label>
                                    <input class="form-control" type="text" id="sample_id" name="sample_id" value="">

                                    <div id="sample_generator"></div>
                                </div>
                            </div>

                            <div class="col-6">
                                <div class="form-group" id="specimen-source-container">
                                    <label for="specimen_source_id">
                                        <span>${ui.message("SAMPLE TYPE")}</span>
                                    </label>
                                    <select class="form-control" name="specimen_source_id" id="specimen_source_id">
                                    </select>
                                    <span class="field-error" style="display: none;"></span>

                                    <div id="error-specimen-source">${ui.message("patientqueueing.select.error")}</div>
                                </div>
                            </div>
                        </div>
                        <br/><br/>

                        <div class="row" style="margin-top: 20px">
                            <div class="col-6">
                                <label for="refer_test">
                                    <span>${ui.message("REFER TEST")}</span>
                                </label>

                                <div class="form-group">
                                    <input type="checkbox" name="refer_test" id="refer_test">

                                    <div class="field-error"
                                         style="display: none;">${ui.message("patientqueueing.select.error")}</div>
                                </div>
                            </div>

                            <div class="col-6" id="reference-lab-container">
                                <div class="form-group">
                                    <select class="form-control" name="reference_lab" id="reference_lab">
                                        <option value="">${ui.message("Select Reference Lab")}</option>
                                        <option value="cphl">CPHL</option>
                                        <option value="uvri">UVRI</option>
                                        <option value="uvri">Other health center Lab</option>
                                        <option value="other-systems">Other Lab Systems</option>
                                    </select>

                                    <div class="field-error"
                                         style="display: none;"><${ui.message("patientqueueing.select.error")}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button class="cancel" data-dismiss="modal"
                                id="">${ui.message("patientqueueing.close.label")}</button>
                        <button type="submit" class="confirm"
                                id="submit-schedule">${ui.message("patientqueueing.send.label")}</button>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>